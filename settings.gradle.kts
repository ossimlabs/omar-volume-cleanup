rootProject.name = "omar-volume-cleanup"

val downloadMavenUrl: String by settings
pluginManagement {
    repositories {
        maven(url = downloadMavenUrl)
    }
}