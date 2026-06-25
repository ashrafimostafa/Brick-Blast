pluginManagement {
    repositories {
        maven(url = "https://maven.myket.ir")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = "https://maven.myket.ir")
        google()
        mavenCentral()
    }
}

rootProject.name = "BrickBlast"
include(":app")
