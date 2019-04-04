package io.ossim.omar.apps.volume.cleanup.app

import io.ktor.application.call
import io.ktor.client.engine.apache.Apache
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ossim.omar.apps.volume.cleanup.log
import io.ossim.omar.apps.volume.cleanup.raster.RasterClient
import io.ossim.omar.apps.volume.cleanup.raster.SizeRestrictedRasterVolume
import io.ossim.omar.apps.volume.cleanup.raster.database.RasterDatabase
import kotlinx.coroutines.time.delay
import java.io.File
import java.util.*

suspend fun main() {
    val config = Configuration()
    log("Starting omar-volume-cleanup with configuration:\n$config")

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

    embeddedServer(Netty, port = 8080) {
        routing {
            get("/health") {
                call.respondText("[${Date()}] I'm alive", ContentType.Text.Plain, HttpStatusCode.OK)
            }
        }
    }.start()

    while (true) {
        log("Restricting volume size")
        sizeRestrictedRasterVolume.cleanVolume()
        delay(config.delay)
    }
}