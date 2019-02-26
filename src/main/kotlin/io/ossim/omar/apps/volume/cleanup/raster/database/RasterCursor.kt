package io.ossim.omar.apps.volume.cleanup.raster.database

import io.ossim.omar.apps.volume.cleanup.raster.RasterEntry
import kotlinx.io.core.Closeable
import java.sql.Connection

class RasterCursor(conn: Connection) : Closeable, Sequence<RasterEntry> {

    @Suppress("SqlResolve")
    private val query = "SELECT image_id, filename FROM raster_entry ORDER BY ingest_date ASC;"
    private val statement = conn.createStatement()
    private val resultSet = statement.executeQuery(query)

    override fun iterator(): Iterator<RasterEntry> = sequence {
        while (!resultSet.isClosed && resultSet.next()) {
            val imageId = resultSet.getString("image_id")
            val filename = resultSet.getString("filename")
            yield(RasterEntry(imageId, filename))
        }
    }.iterator()

    override fun close() {
        resultSet.close()
        statement.close()
    }
}