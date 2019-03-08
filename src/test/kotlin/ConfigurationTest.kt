import io.ossim.omar.apps.volume.cleanup.app.Configuration
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class ConfigurationTest {
    private val testValues = object {
        val dryRun = true
        val volumeDir = "/raster"
        val delay = Duration.of(10, ChronoUnit.MINUTES)
        val percent = 0.95
        val rasterEndpoint = "https://test-endpoint/raster/"
        val databaseUrl = "jdbc://test/db/url"
        val databaseName = "test_name"
        val databasePass = "test_pass"
    }

    @BeforeTest
    fun `Set virtual environment variables`() {
        setEnv(
            mapOf(
                "CLEANUP_DRYRUN" to testValues.dryRun.toString(), // Default to true to avoid accidental deletions
                "CLEANUP_VOLUME" to testValues.volumeDir,
                "CLEANUP_DELAY" to testValues.delay.toString(), // Ten minute default
                "CLEANUP_PERCENT" to testValues.percent.toString(),
                "CLEANUP_RASTERENDPOINT" to testValues.rasterEndpoint,
                "DATABASE_URL" to testValues.databaseUrl,
                "DATABASE_USERNAME" to testValues.databaseName,
                "DATABASE_PASSWORD" to testValues.databasePass
            )
        )
    }

    @Test
    fun `Configuration values loaded from environment variables`() {

        val config = Configuration()

        assertEquals(testValues.dryRun, config.dryRun)
        assertEquals(testValues.volumeDir, config.volume)
        assertEquals(testValues.delay, config.delay)
        assertEquals(testValues.percent, config.percentThreshold)
        assertEquals(testValues.rasterEndpoint, config.rasterEndpoint)
        assertEquals(testValues.databaseUrl, config.databaseUrl)
        assertEquals(testValues.databaseName, config.databaseUsername)
        assertEquals(testValues.databasePass, config.databasePassword)
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