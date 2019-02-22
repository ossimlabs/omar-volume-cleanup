import java.io.Closeable
import java.sql.Connection

/**
 * Represents the RasterEntry results cursor retrieved from the [database][dbConnection] by the [SQL statement][query]
 * "SELECT image_id, filename FROM raster_entry ORDER BY ingest_date ASC;"
 *
 * [Rasters][rasters] are lazily retrieved from the [resultSet] cursor.
 *
 * This should be [closed][close] in order to release the cursor resources.
 */
class DatabaseRasterCursor(dbConnection: Connection) : Closeable {
    private val query = "SELECT image_id, filename FROM raster_entry ORDER BY ingest_date ASC;"
    private val resultSet = dbConnection.createStatement().executeQuery(query)

    val rasters = sequence {
        while (!resultSet.isClosed && resultSet.next()) {
            val imageId = resultSet.getString("image_id")
            val filename = resultSet.getString("filename")
            yield(RasterEntry(imageId, filename))
        }
    }

    override fun close() {
        resultSet.close()
    }
}