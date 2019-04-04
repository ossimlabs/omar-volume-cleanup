package io.ossim.omar.apps.volume.cleanup.raster

import io.ossim.omar.apps.volume.cleanup.humanReadableByteCount
import io.ossim.omar.apps.volume.cleanup.log
import io.ossim.omar.apps.volume.cleanup.raster.database.RasterDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.File

/**
 * Represents a size restricted raster volume mounted at a local [volumeDir], connected to a [database],
 * and accessible through the provided [client].
 *
 * The [volume][volumeDir] size is restricted to a percentage of [usable space][File.getUsableSpace]
 * specified by the percent threshold (value between 0 and 1).
 *
 * Raster files are deleted in concurrently using the [client] until the size threshold is reached.
 * Exceptions thrown when removing a raster is caught and logged without stopping the removal of other rasters.
 *
 * @param volumeDir An existing directory, most likely a partition root
 * @param client The [RasterClient] to be used when removing rasters
 * @param database The [RasterDatabase] that will be searched when selecting rasters for removal
 * @param percentThreshold The percentage (0.0 to 1.0) of the [usable space][File.getUsableSpace] that may be used by the [volumeDir]
 * @param dryRun If true, rasters will not be removed
 *
 * @see CoroutineScope
 */
class SizeRestrictedRasterVolume(
    private val volumeDir: File,
    private val client: RasterClient,
    private val database: RasterDatabase,
    percentThreshold: Double,
    private val dryRun: Boolean = false
) {

    private val bytesThreshold: Long = (volumeDir.totalSpace * percentThreshold).toLong()

    init {
        require(percentThreshold in 0.0..1.0) { "Percent threshold $percentThreshold is not within range 0 to 1" }
        check(volumeDir.totalSpace > 0) { "Volume ${volumeDir.absolutePath} has no usable space" }
    }

    /**
     * Removes rasters while the used space is over the threshold.
     * The used space is checked against the threshold only once per call.
     *
     * Exceptions thrown when removing rasters are collected and logged.
     * The database cursor opened for the duration of the method.
     */
    suspend fun cleanVolume() = coroutineScope {
        val bytesOverThreshold = (volumeDir.totalSpace - volumeDir.usableSpace) - bytesThreshold
        log(
            """
            Cleaning volume: $volumeDir
            Total space: ${volumeDir.totalSpace}
            Usable space: ${volumeDir.usableSpace}
            Threshold: ${bytesOverThreshold.humanReadableByteCount()}
            Bytes over threshold: ${bytesOverThreshold.humanReadableByteCount()}
        """.trimIndent()
        )

        if (bytesOverThreshold > 0) {
            database.rasterCursor().use { rasters ->
                rasters
                    .takeWhileByteSumIsLessThan(bytesOverThreshold)
                    // We want to limit our number of concurrent requests so not to overload downstream systems.
                    .chunked(100)
                    .forEach { chunk ->
                        chunk.map { raster ->
                            launch { tryRemoveRaster(raster) }
                        }.joinAll()
                    }
            }
        }
    }

    /**
     * Tries to remove the [RasterEntry] using the [client]. Failures to remove the raster are caught and logged.
     */
    private suspend fun tryRemoveRaster(raster: RasterEntry) {
        val dryRunMessage = if (dryRun) "[DRY RUN] " else ""
        val rasterSize = raster.length.humanReadableByteCount()

        val removal = runCatching {
            if (!dryRun) client.remove(raster)
        }

        val removalLogMessage =
            if (removal.isSuccess) "Removed $rasterSize $raster "
            else "Failed to remove $raster with exception: ${removal.exceptionOrNull()}"

        log(dryRunMessage + removalLogMessage)
    }

    /**
     * Takes rasters while the total accumulated file size in bytes is less than the given [bytes].
     */
    private fun Sequence<RasterEntry>.takeWhileByteSumIsLessThan(bytes: Long): Sequence<RasterEntry> {
        var byteSum = 0L
        return this
            .map { it to it.length }
            .takeWhile { (_, fileSize) -> (byteSum < bytes).also { byteSum += fileSize } }
            .map { (rasterEntry, _) -> rasterEntry }
    }
}
