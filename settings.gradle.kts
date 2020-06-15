rootProject.name = "omar-volume-cleanup"


pluginManagement {
    val downloadMavenUrl: String by settings
    repositories {
        maven(url = downloadMavenUrl)
    }
}