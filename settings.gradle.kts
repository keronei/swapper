pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    plugins {
        id("com.android.application") version "8.7.3"
        id("com.android.library") version "7.4.1"
        id("com.google.firebase.crashlytics") version "2.5.2"
        id("org.jetbrains.kotlin.android") version "2.1.0"
        id("com.google.dagger.hilt.android") version "2.56.2"
        id("com.google.devtools.ksp") version "2.0.21-1.0.27"
        id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    }
}
include("app")
rootProject.name = "Swapper"