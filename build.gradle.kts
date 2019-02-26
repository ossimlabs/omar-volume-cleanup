import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.20"
    id("com.google.cloud.tools.jib") version "1.0.0"
    `maven-publish`
}

group = "io.ossim.omar.apps"
version = "1.0-SNAPSHOT"

repositories {
    ossimlabsMaven()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
    implementation("io.ktor:ktor-client:1.1.3")
    implementation("org.postgresql:postgresql:42.2.2")
    implementation("com.uchuhimo:konf:0.13.1")
    testImplementation(kotlin("test-junit"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

jib {
    from { image = "fabric8/java-centos-openjdk8-jdk" }
    container.mainClass = "io.ossim.omar.apps.volume.cleanup.app.AppKt"
    container.volumes = listOf("/rasters")
    container.environment = mapOf(
        "CLEANUP_DRY_RUN" to "true", // Default to true to avoid accidental deletions
        "CLEANUP_VOLUME" to "/raster",
        "CLEANUP_DELAY" to "10m", // Ten minute default
        "CLEANUP_PERCENT" to "0.95",
        "CLEANUP_RASTERENDPOINT" to "",
        "DATABASE_URL" to "",
        "DATABASE_USERNAME" to "",
        "DATABASE_PASSWORD" to ""
    )
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        publishMaven()
    }
}

fun RepositoryHandler.ossimlabsMaven() = maven {
    val ossimMavenProxy: String by project
    url = uri(ossimMavenProxy)
}

fun RepositoryHandler.publishMaven() = maven {
    val mavenRepoUrl: String by project
    val mavenRepoUsername: String by project
    val mavenRepoPassword: String by project

    url = uri(mavenRepoUrl)
    credentials {
        username = mavenRepoUsername
        password = mavenRepoPassword
    }
}