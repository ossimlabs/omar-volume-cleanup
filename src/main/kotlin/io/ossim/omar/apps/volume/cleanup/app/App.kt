package io.ossim.omar.apps.volume.cleanup.app

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ossim.omar.apps.volume.cleanup.launchVolumeSizeLogger
import io.ossim.omar.apps.volume.cleanup.log
import io.ossim.omar.apps.volume.cleanup.raster.RasterClient
import io.ossim.omar.apps.volume.cleanup.raster.SizeRestrictedRasterVolume
import io.ossim.omar.apps.volume.cleanup.raster.database.RasterDatabase
import io.ossim.omar.apps.volume.cleanup.startHealthEndpointServer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.time.delay
import java.io.File
import java.time.Duration

suspend fun main() = coroutineScope {
    val config = Configuration()
    log("Starting omar-volume-cleanup with configuration:\n$config")

    val database = RasterDatabase(
        url = config.databaseUrl,
        username = config.databaseUsername,
        password = config.databasePassword
    )

    val volumeFile = File(config.volume)
    val sizeRestrictedRasterVolume = SizeRestrictedRasterVolume(
        volumeDir = volumeFile,
        client = RasterClient(config.rasterEndpoint, HttpClient(Apache)),
        database = database,
        percentThreshold = config.percentThreshold,
        dryRun = config.dryRun
    )

    startHealthEndpointServer(8080)
    launchVolumeSizeLogger(volumeFile, Duration.ofSeconds(30))

    while (true) {
        log("Restricting volume size")
        sizeRestrictedRasterVolume.cleanVolume()
        delay(config.delay)
    }
}