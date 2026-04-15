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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Purefin"
include(":app")
include(":app-tv")
include(":core:model")
include(":core:image")
include(":core:ui")
include(":core:data")
include(":core:navigation")
include(":core:download")
include(":core:player")
include(":data:jellyfin")
include(":data:offline")
include(":data:catalog")
include(":feature:browse")
include(":feature:search")
include(":feature:content")
include(":feature:downloads")
include(":feature:login")
