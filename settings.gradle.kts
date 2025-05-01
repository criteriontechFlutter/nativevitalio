pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io") // ✅ Must be here
        flatDir {
            dirs("libs")
        }
    }
}

rootProject.name = "nativevitalio"
include(":app")
include( ":omronconnectivitylibrary")
include(":omronconnectivitylibraryassets")

 