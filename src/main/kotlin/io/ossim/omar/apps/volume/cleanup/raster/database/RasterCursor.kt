package io.ossim.omar.apps.volume.cleanup.raster.database

import io.ossim.omar.apps.volume.cleanup.log
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
            val imageId: String? = resultSet.getString("image_id")
            val filename: String = resultSet.getString("filename")

            if (imageId != null) {
                yield(RasterEntry(imageId, filename))
            } else {
                // Null IDs are possible.
                log("Skipping image with null ID: $filename")
            }
        }
    }.iterator()

    override fun close() {
        resultSet.close()
        statement.close()
    }
}