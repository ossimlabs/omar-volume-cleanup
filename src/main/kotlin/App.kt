import com.uchuhimo.konf.Config
import kotlinx.coroutines.*
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

val config = Config { addSpec(DiskCleanupSpec); addSpec(DatabaseSpec) }
    .from.yaml.file("config.yml")
    .from.json.resource("config.json")
    .from.env()
    .from.systemProperties()

suspend fun main() {
    val volume: String = config[DiskCleanupSpec.volume]
    val delayMillis: Long = config[DiskCleanupSpec.delayMillis]
    val rasterEndpoint: String = config[DiskCleanupSpec.rasterEndpoint]
    val percentThreshold: Double = config[DiskCleanupSpec.percentThreshold]

    SizeRestrictedRasterVolume(
        volumeDir = File(volume),
        endpoint = rasterEndpoint,
        dbConnection = getDatabaseConnection(),
        percentThreshold = percentThreshold
    ).use {
        while (true) {
            it.cleanVolume()
            delay(delayMillis)
        }
    }
}

private fun getDatabaseConnection(): Connection {
    val username = config[DatabaseSpec.username]
    val password = config[DatabaseSpec.password]
    val url = config[DatabaseSpec.url]

    val dbProps = Properties().apply {
        put("username", username)
        put("password", password)
    }
    return DriverManager.getConnection(url, dbProps)
}
