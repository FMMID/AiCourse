pluginManagement {
    repositories {
        gradlePluginPortal() // <--- Вот это самое главное!
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "notification"
