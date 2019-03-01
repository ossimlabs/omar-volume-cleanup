package io.ossim.omar.apps.volume.cleanup.app

import com.uchuhimo.konf.Config
import io.ktor.client.engine.apache.Apache
import io.ossim.omar.apps.volume.cleanup.raster.RasterClient
import io.ossim.omar.apps.volume.cleanup.raster.SizeRestrictedRasterVolume
import io.ossim.omar.apps.volume.cleanup.raster.database.RasterDatabase
import kotlinx.coroutines.time.delay
import java.io.File

val config = Config { addSpec(CleanupSpec); addSpec(DatabaseSpec) }
    .from.env()
    .from.systemProperties()

suspend fun main() {

    // --- Configuration ---
    val delay = config[CleanupSpec.delay]
    val percentThreshold = config[CleanupSpec.percentThreshold]
    val volumeDir = File(config[CleanupSpec.volume])

    val httpEngine = Apache.create()
    val client = RasterClient(config[CleanupSpec.rasterEndpoint], httpEngine)

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
        delay(delay)
    }
}