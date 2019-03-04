package io.ossim.omar.apps.volume.cleanup.app

import io.ktor.client.engine.apache.Apache
import io.ossim.omar.apps.volume.cleanup.raster.RasterClient
import io.ossim.omar.apps.volume.cleanup.raster.SizeRestrictedRasterVolume
import io.ossim.omar.apps.volume.cleanup.raster.database.RasterDatabase
import kotlinx.coroutines.time.delay
import java.io.File

suspend fun main() {

    // --- Configuration ---
    val delay = Configuration.delay
    val percentThreshold = Configuration.percentThreshold
    val volumeDir = File(Configuration.volume)

    val httpEngine = Apache.create()
    val client = RasterClient(Configuration.rasterEndpoint, httpEngine)

    val database = RasterDatabase(
        url = Configuration.databaseUrl,
        username = Configuration.databaseUsername,
        password = Configuration.databasePassword
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