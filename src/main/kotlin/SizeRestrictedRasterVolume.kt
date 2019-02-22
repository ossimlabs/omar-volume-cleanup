import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.url
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.*
import java.io.Closeable
import java.io.File
import java.sql.Connection
import kotlin.coroutines.CoroutineContext

/**
 * Represents a size restricted raster volume mounted at a local [volumeDir], connected to a [database][dbConnection],
 * and accessible at a URL [endpoint].
 *
 * The [volume][volumeDir] size is restricted to a percentage of [usable space][File.getUsableSpace]
 * specified by a [percentThreshhold] between 0.0 and 1.0.
 *
 * Raster files are deleted using the raster URL [endpoint] by order of their ingest date.
 * Files are deleted until the threshold is reached.
 *
 * Exceptions thrown when removing rasters are caught and logged per raster.
 *
 * Calls to removeRasterEndpoint are done concurrently using the [Dispatchers.IO] context.
 * Coroutines are launched within the [SizeRestrictedRasterVolume]'s [scope][CoroutineScope] to avoid concurrency leaks.
 *
 * @param volumeDir An existing directory, most likely a partition root
 * @param endpoint The root raster URL endpoint that will be used when removing rasters (the URL should not include the /removeRaster path)
 * @param dbConnection The connection to the Raster database that has the raster_entry table
 * @param percentThreshold The percentage (0.0 to 1.0) of the [usable space][File.getUsableSpace] that may be used by the [volumeDir]
 * @param dryRun If true, no call will be made to the removeRaster [endpoint] for testing purposes
 *
 * @see CoroutineScope
 */
class SizeRestrictedRasterVolume(
    private val volumeDir: File,
    private val endpoint: String,
    private val dbConnection: Connection,
    percentThreshold: Double,
    private val dryRun: Boolean = false
) : Closeable {

    private val bytesThreshold: Long = (volumeDir.totalSpace * percentThreshold).toLong()
    private val client = HttpClient()

    init {
        require(percentThreshold in 0.0..1.0) { "Percent threshold $percentThreshold is not within range 0 to 1" }
        require(volumeDir.exists()) { "Volume ${volumeDir.absolutePath} does not exist" }
        require(volumeDir.isDirectory) { "Volume ${volumeDir.absolutePath} is not a directory" }
        check(volumeDir.totalSpace > 0) { "Volume ${volumeDir.absolutePath} has no usable space" }
        check(dbConnection.isValid(1)) { "Database connection is not valid" }
    }

    /**
     * Removes rasters while the used space is over the threshold.
     * The used space is checked against the threshold only once per call.
     *
     * Exceptions thrown when removing rasters are collected and logged.
     * The database cursor opened for the duration of the method.
     */
    suspend fun cleanVolume() = coroutineScope {
        val bytesOverThreshold = volumeDir.usableSpace - bytesThreshold

        if (bytesOverThreshold > 0) {
            DatabaseRasterCursor(dbConnection).use { databaseRastersResult ->
                databaseRastersResult.rasters
                    .takeWhileByteSumIsLessThan(bytesOverThreshold)
                    .forEach { launch { it.removeCatching() } }
            }
        }
    }

    /**
     * Tries to remove the [RasterEntry]. Failures to remove the raster are caught and logged.
     */
    private suspend fun RasterEntry.removeCatching() {
        val deleteResult = runCatching {
            remove()
        }

        if (deleteResult.isSuccess) {
            log("Removed ${deleteResult.getOrThrow()}")
        } else {
            log("Failed to remove ${deleteResult.getOrThrow()} with exception: ${deleteResult.exceptionOrNull()}")
        }
    }

    /**
     * Removes the [RasterEntry] using the removeRaster [endpoint]. An [Exception] is thrown if
     * the HTTP response status is not [200][HttpStatusCode.OK].
     *
     * The call takes place within the [Dispatchers.IO] [CoroutineContext] for minimal blocking.
     *
     * @throws Exception when the removeRaster response status is not [200][HttpStatusCode.OK]
     */
    private suspend fun RasterEntry.remove() = withContext(Dispatchers.IO) {
        log("Removing raster $this")
        if (!dryRun) {
            val call = client.call {
                url("$endpoint/dataManager/removeRaster?deleteFiles=true&filename=$filename")
                method = HttpMethod.Post
            }
            if (call.response.status != HttpStatusCode.OK) throw Exception(
                "Failed to delete raster $filename at URL ${call.request.url}",
                Exception(call.response.readText())
            )
        }
        log("Removed raster ${this@remove}")
    }

    override fun close() {
        client.close()
        dbConnection.close()
    }
}

private fun Sequence<RasterEntry>.takeWhileByteSumIsLessThan(bytes: Long): Sequence<RasterEntry> {
    var byteSum = 0L
    return this
        .map { it to it.fileSize() }
        .takeWhile { (_, fileSize) -> (byteSum < bytes).also { byteSum += fileSize } }
        .map { (rasterEntry, _) -> rasterEntry }
}

private fun log(message: String) {
    println(message)
}
