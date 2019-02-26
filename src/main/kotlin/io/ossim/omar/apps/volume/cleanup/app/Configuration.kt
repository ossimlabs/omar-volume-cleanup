package io.ossim.omar.apps.volume.cleanup.app

import com.uchuhimo.konf.ConfigSpec

object DiskCleanupSpec : ConfigSpec("diskcleanup") {
    val volume by required<String>()
    val delayMillis by required<Long>()
    val rasterEndpoint by required<String>()
    val percentThreshold by required<Double>()
}

object DatabaseSpec : ConfigSpec("database") {
    val driver by optional("org.postgresql.Driver")
    val url by required<String>()
    val username by required<String>()
    val password by required<String>()
}