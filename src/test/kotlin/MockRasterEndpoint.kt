import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockHttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.fullPath
import io.ktor.http.hostWithPort
import kotlinx.coroutines.io.ByteReadChannel

class MockRasterEndpoint(val rasterUrl: String, val fails: Boolean = false) {
    var hits = 0

    val engine = MockEngine {
        return@MockEngine if (url.fullUrl.startsWith(rasterUrl) && !fails) {
            hits++
            MockHttpResponse(call, HttpStatusCode.OK, ByteReadChannel(hits.toString()))
        } else {
            MockHttpResponse(call, HttpStatusCode.BadRequest, ByteReadChannel(hits.toString()))
        }
    }

    private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"
    private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
}