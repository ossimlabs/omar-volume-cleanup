import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion by ext("1.1.3")
val downloadMavenUrl: String by project
val uploadMavenRepoUrl: String by project
val uploadMavenRepoUsername: String by project
val uploadMavenRepoPassword: String by project

plugins {
    kotlin("jvm") version "1.3.21"
    `maven-publish`
    id("com.google.cloud.tools.jib") version "1.0.0"
    id("org.sonarqube") version "2.7"
}

group = "io.ossim.omar.apps"
version = "1.0-SNAPSHOT"

repositories {
    maven(downloadMavenUrl)
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
    implementation("io.ktor:ktor-client:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("org.postgresql:postgresql:42.2.2")
    implementation("com.uchuhimo:konf:0.13.1")
    testImplementation(kotlin("test-junit"))
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    testImplementation("io.mockk:mockk:1.9.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

jib {
    container.mainClass = "io.ossim.omar.apps.volume.cleanup.app.AppKt"
    container.volumes = listOf("/data")
    container.environment = mapOf(
        "CLEANUP_DRYRUN" to "true", // Default to true to avoid accidental deletions
        "CLEANUP_VOLUME" to "/data",
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
        maven(uploadMavenRepoUrl) {
            credentials {
                username = uploadMavenRepoUsername
                password = uploadMavenRepoPassword
            }
        }
    }
}