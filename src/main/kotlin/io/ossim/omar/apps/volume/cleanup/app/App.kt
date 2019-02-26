package io.ossim.omar.apps.volume.cleanup.app

import com.uchuhimo.konf.Config
import io.ossim.omar.apps.volume.cleanup.raster.RasterClient
import io.ossim.omar.apps.volume.cleanup.raster.SizeRestrictedRasterVolume
import io.ossim.omar.apps.volume.cleanup.raster.database.RasterDatabase
import kotlinx.coroutines.delay
import java.io.File

val config = Config { addSpec(DiskCleanupSpec); addSpec(DatabaseSpec) }
    .from.yaml.file("application.yml")
    .from.json.resource("application.json")
    .from.env()
    .from.systemProperties()

suspend fun main() {

    // --- Configuration ---
    val delayMillis = config[DiskCleanupSpec.delayMillis]
    val percentThreshold = config[DiskCleanupSpec.percentThreshold]
    val volumeDir = File(config[DiskCleanupSpec.volume])
    val client = RasterClient(config[DiskCleanupSpec.rasterEndpoint])
    val database = RasterDatabase(
        url = config[DatabaseSpec.url],
        username = config[DatabaseSpec.username],
        password = config[DatabaseSpec.password]
    )


    // --- Application ---
    val sizeRestrictedRasterVolume = SizeRestrictedRasterVolume(
        volumeDir = volumeDir,
        client = client,
        database = database,
        percentThreshold = percentThreshold
    )

    while (true) {
        sizeRestrictedRasterVolume.cleanVolume()
        delay(delayMillis)
    }
}