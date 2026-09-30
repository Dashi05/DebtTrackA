pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Required for MPAndroidChart (statistics screen)
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "DebtTrack"
include(":app")
