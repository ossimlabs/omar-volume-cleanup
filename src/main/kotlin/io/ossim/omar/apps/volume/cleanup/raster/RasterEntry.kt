package io.ossim.omar.apps.volume.cleanup.raster

import java.io.File

/**
 * Immutable representation of a [RasterEntry]
 */
data class RasterEntry(val imageId: String, val filename: String) {
    /**
     * The length of bytes for the raster or 0L if the raster file does not exist.
     * @see File.length
     */
    val length = File(filename).length()
}