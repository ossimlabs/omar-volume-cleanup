import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.20"
    id("com.google.cloud.tools.jib") version "1.0.0"
}

group = "io.ossim.omar.apps"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
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
    container.mainClass = "AppKt.class"
}