import io.ktor.client.HttpClient
import io.ossim.omar.apps.volume.cleanup.raster.RasterClient
import io.ossim.omar.apps.volume.cleanup.raster.RasterEntry
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class RasterClientTest {
    @Test
    fun `Remove raster hits provided endpoint`() {
        val rasterUrl = "http://test/raster/client/url"
        val rasterServer = MockRasterEndpoint(rasterUrl)
        val client = RasterClient(rasterUrl, HttpClient(rasterServer.engine))

        runBlocking {
            (1..3).forEach {
                client.remove(RasterEntry("image-id-stub", "filename-stub"))
                assertEquals(it, rasterServer.hits)
            }
        }
    }

    @Test
    fun `Remove raster fails with proper exception`() {
        val rasterUrl = "http://test/raster/client/url"
        val rasterServer = MockRasterEndpoint(rasterUrl, fails = true)
        val client = RasterClient(rasterUrl, HttpClient(rasterServer.engine))

        assertFails {
            runBlocking {
                client.remove(RasterEntry("image-id-stub", "filename-stub"))
            }
        }
    }
}