package io.ossim.omar.apps.volume.cleanup.raster

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RasterClient(val rasterUrl: String) {

    /**
     * Removes the raster using the removeRaster endpoint at the given [url][rasterUrl].
     * An [Exception] is thrown if the call does not return [200 OK][HttpStatusCode.OK].
     */
    suspend fun remove(raster: RasterEntry) = withContext(Dispatchers.IO) {
        HttpClient().use { client ->
            val call = client.call {
                url("$rasterUrl/dataManager/removeRaster?deleteFiles=true&filename=${raster.filename}")
                method = HttpMethod.Post
            }
            if (call.response.status != HttpStatusCode.OK) throw Exception(
                "Failed to delete raster ${raster.filename} at URL ${call.request.url}",
                Exception("${call.response.status}: ${call.response.status.description}")
            )

            println("Removed raster $raster")
        }
    }
}
