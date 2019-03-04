import io.ossim.omar.apps.volume.cleanup.app.Configuration
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class ConfigurationTest {

    @Test
    fun `Cleanup config values loaded environment variables`() {
        val testDryRun = true
        val testVolumeDir = "/raster"
        val testDelay = Duration.of(10, ChronoUnit.MINUTES)
        val testPercent = 0.95
        val testRasterEndpoint = "https://test-endpoint/raster/"
        val testDatabaseUrl = "jdbc://test/db/url"
        val testDatabaseName = "test_name"
        val testDatabasePass = "test_pass"

        setEnv(
            mapOf(
                "CLEANUP_DRYRUN" to testDryRun.toString(), // Default to true to avoid accidental deletions
                "CLEANUP_VOLUME" to testVolumeDir,
                "CLEANUP_DELAY" to testDelay.toString(), // Ten minute default
                "CLEANUP_PERCENT" to testPercent.toString(),
                "CLEANUP_RASTERENDPOINT" to testRasterEndpoint,
                "DATABASE_URL" to testDatabaseUrl,
                "DATABASE_USERNAME" to testDatabaseName,
                "DATABASE_PASSWORD" to testDatabasePass
            )
        )

        assertEquals(testDryRun, Configuration.dryRun)
        assertEquals(testVolumeDir, Configuration.volume)
        assertEquals(testDelay, Configuration.delay)
        assertEquals(testPercent, Configuration.percentThreshold)
        assertEquals(testRasterEndpoint, Configuration.rasterEndpoint)
        assertEquals(testDatabaseUrl, Configuration.databaseUrl)
        assertEquals(testDatabaseName, Configuration.databaseUsername)
        assertEquals(testDatabasePass, Configuration.databasePassword)
    }

    /**
     * Set the environment variables in JVM memory.
     *
     * Normally, you would *not* want to set environment variables in JVM/Java since it's not platform neutral.
     *
     * This is good for testing as the environment variables only exist in memory and do not effect the host,
     * therefore this method is platform neutral.
     *
     * @see "https://stackoverflow.com/a/7201825/2832996"
     */
    @Suppress("UNCHECKED_CAST")
    private fun setEnv(newenv: Map<String, String>) {
        try {
            val processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment")
            val theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment")
            theEnvironmentField.isAccessible = true
            val env = theEnvironmentField.get(null) as MutableMap<String, String>
            env.putAll(newenv)
            val theCaseInsensitiveEnvironmentField =
                processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment")
            theCaseInsensitiveEnvironmentField.isAccessible = true
            val cienv = theCaseInsensitiveEnvironmentField.get(null) as MutableMap<String, String>
            cienv.putAll(newenv)
        } catch (e: NoSuchFieldException) {
            val classes = Collections::class.java.declaredClasses
            val env = System.getenv()
            for (cl in classes) {
                if ("java.util.Collections\$UnmodifiableMap" == cl.name) {
                    val field = cl.getDeclaredField("m")
                    field.isAccessible = true
                    val obj = field.get(env)
                    val map = obj as MutableMap<String, String>
                    map.clear()
                    map.putAll(newenv)
                }
            }
        }
    }
}