package io.ossim.omar.apps.volume.cleanup

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.io.File
import java.sql.ResultSet
import java.sql.SQLException
import java.time.Duration
import java.util.*

/**
 * Returns the receiver in byte text format (e.g. 3.2 KiB)
 * @param si Use decimal prefixes (SI) (e.g. kB) instead of binary prefixes (IEC) (e.g. KiB)
 * https://stackoverflow.com/a/3758880/2832996
 */
internal fun Long.humanReadableByteCount(si: Boolean = false): String {
    val unit = if (si) 1000 else 1024
    if (this < unit) return "$this B"
    val exp = (Math.log(toDouble()) / Math.log(unit.toDouble())).toInt()
    val prefix = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
    return String.format("%.1f %sB", this / Math.pow(unit.toDouble(), exp.toDouble()), prefix)
}

internal fun log(message: String) = println("[${Date()}] $message")

internal fun logMetrics(metrics: Map<String, String>) {
    val jsonObject = metrics
        .map { (k, v) -> "\"$k\": \"$v\"" }
        .joinToString(separator = ",", prefix = "{", postfix = "}")
    println(jsonObject)
}

/**
 * Print a result set to system out.
 *
 * @param rs The ResultSet to print
 * @throws SQLException If there is a problem reading the ResultSet
 */
internal fun printResultSet(rs: ResultSet) {

    // Prepare metadata object and get the number of columns.
    val rsmd = rs.metaData
    val columnsNumber = rsmd.columnCount

    // Print column names (a header).
    for (i in 1..columnsNumber) {
        if (i > 1) print(" | ")
        print(rsmd.getColumnName(i))
    }
    println("")

    while (rs.next()) {
        for (i in 1..columnsNumber) {
            if (i > 1) print(" | ")
            print(rs.getString(i))
        }
        println("")
    }
}

internal fun CoroutineScope.launchVolumeSizeLogger(volume: File, reportInterval: Duration) = launch {
    while (isActive) {
        logMetrics(
            mapOf(
                "volume" to volume.name,
                "totalSpace" to volume.totalSpace.toString(),
                "usableSpace" to volume.usableSpace.toString()
            )
        )
        delay(reportInterval)
    }
}

internal fun startHealthEndpointServer(port: Int): ApplicationEngine {
    return embeddedServer(
        Netty,
        port = port
    ) {
        routing {
            get("/health") {
                call.respondText(
                    "[${Date()}] I'm alive",
                    ContentType.Text.Plain,
                    HttpStatusCode.OK
                )
            }
        }
    }.start()
}