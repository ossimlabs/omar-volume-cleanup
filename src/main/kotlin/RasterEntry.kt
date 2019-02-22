import java.io.File

/**
 * Immutable representation of a [RasterEntry]
 */
data class RasterEntry(val imageId: String, val filename: String)

/**
 * Utility method that returns the [total space][File.getTotalSpace] of a RasterEntry's file.
 */
fun RasterEntry.fileSize(): Long = File(filename).totalSpace