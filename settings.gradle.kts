pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://api.xposed.info/repository/repository")
    }
}

rootProject.name = "LsposedUITemplate"
include(":app")
