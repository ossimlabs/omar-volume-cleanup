package io.ossim.omar.apps.volume.cleanup.app

import com.uchuhimo.konf.ConfigSpec
import java.time.Duration

object CleanupSpec : ConfigSpec() {
    val volume by required<String>()
    val delay by required<Duration>()
    val rasterEndpoint by required<String>()
    val percentThreshold by required<Double>("percent")
}

object DatabaseSpec : ConfigSpec() {
    val url by required<String>()
    val username by required<String>()
    val password by required<String>()
}