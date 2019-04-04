package io.ossim.omar.apps.volume.cleanup.raster.database

import io.ossim.omar.apps.volume.cleanup.log
import kotlinx.io.core.Closeable
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class RasterDatabase(val url: String, val username: String, val password: String) : Closeable {
    private val conn: Connection

    init {
        val dbProps = Properties().apply {
            put("user", username)
            put("password", password)
        }
        conn = DriverManager.getConnection(url, dbProps)
        log("Connected to database")
    }

    fun rasterCursor() = RasterCursor(conn)

    override fun close() {
        conn.close()
        log("Closed database connection")
    }
}