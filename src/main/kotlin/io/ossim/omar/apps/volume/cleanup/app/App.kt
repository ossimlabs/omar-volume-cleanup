package io.ossim.omar.apps.volume.cleanup.app

import io.ktor.client.engine.apache.Apache
import io.ossim.omar.apps.volume.cleanup.raster.RasterClient
import io.ossim.omar.apps.volume.cleanup.raster.SizeRestrictedRasterVolume
import io.ossim.omar.apps.volume.cleanup.raster.database.RasterDatabase
import kotlinx.coroutines.time.delay
import java.io.File

suspend fun main() {
    val config = Configuration()

    val database = RasterDatabase(
        url = config.databaseUrl,
        username = config.databaseUsername,
        password = config.databasePassword
    )

    val sizeRestrictedRasterVolume = SizeRestrictedRasterVolume(
        volumeDir = File(config.volume),
        client = RasterClient(config.rasterEndpoint, Apache.create()),
        database = database,
        percentThreshold = config.percentThreshold,
        dryRun = config.dryRun
    )

    while (true) {
        sizeRestrictedRasterVolume.cleanVolume()
        delay(config.delay)
    }
}