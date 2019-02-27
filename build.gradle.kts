import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.20"
    `maven-publish`
    id("com.google.cloud.tools.jib") version "1.0.0"
    id("org.sonarqube") version "2.7"
}

group = "io.ossim.omar.apps"
version = "1.0-SNAPSHOT"

repositories {
    downloadMaven()
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
        uploadMaven()
    }
}

fun RepositoryHandler.downloadMaven() = maven {
    val downloadMavenUrl: String by project
    url = uri(downloadMavenUrl)
}

fun RepositoryHandler.uploadMaven() = maven {
    val uploadMavenRepoUrl: String by project
    val uploadMavenRepoUsername: String by project
    val uploadMavenRepoPassword: String by project

    url = uri(uploadMavenRepoUrl)
    credentials {
        username = uploadMavenRepoUsername
        password = uploadMavenRepoPassword
    }
}