package io.ossim.omar.apps.volume.cleanup.app

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import java.time.Duration

object Configuration {
    private val config = Config { addSpec(CleanupSpec); addSpec(DatabaseSpec) }
        .from.env()
        .from.systemProperties()

    val volume get() = config[CleanupSpec.volume]
    val delay get() = config[CleanupSpec.delay]
    val rasterEndpoint get() = config[CleanupSpec.rasterEndpoint]
    val percentThreshold get() = config[CleanupSpec.percentThreshold]
    val dryRun get() = config[CleanupSpec.dryRun]

    val databaseUrl get() = config[DatabaseSpec.url]
    val databaseUsername get() = config[DatabaseSpec.username]
    val databasePassword get() = config[DatabaseSpec.password]
}

private object CleanupSpec : ConfigSpec() {
    val volume by required<String>()
    val delay by required<Duration>()
    val rasterEndpoint by required<String>()
    val percentThreshold by required<Double>("percent")
    val dryRun by optional(false)
}

private object DatabaseSpec : ConfigSpec() {
    val url by required<String>()
    val username by required<String>()
    val password by required<String>()
}