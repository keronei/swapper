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
        id("com.google.devtools.ksp") version "1.9.24-1.0.20"
    }
}
include("app")
rootProject.name = "GradlePlugins"