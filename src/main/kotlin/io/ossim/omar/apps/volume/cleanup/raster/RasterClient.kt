package io.ossim.omar.apps.volume.cleanup.raster

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.HttpClientEngine
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class RasterClient(private val rasterUrl: String, private val httpClientEngine: HttpClientEngine) {

    /**
     * Removes the raster using the removeRaster endpoint at the given [url][rasterUrl].
     * An [Exception] is thrown if the call does not return [200 OK][HttpStatusCode.OK].
     */
    suspend fun remove(raster: RasterEntry) = HttpClient(httpClientEngine).use { client ->
        val call = client.call(removeRasterUrl(filename = raster.filename)) {
            method = HttpMethod.Post
        }

        if (call.response.status != HttpStatusCode.OK) throw Exception(
            "Failed to delete raster at URL ${call.request.url}",
            Exception("${call.response.status}: ${call.response.status.description}")
        )
    }

    private fun removeRasterUrl(filename: String) =
        "$rasterUrl/dataManager/removeRaster?deleteFiles=true&filename=$filename"
}