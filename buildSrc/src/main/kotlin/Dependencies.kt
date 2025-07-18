object Versions {

    //Version codes for all the libraries
    const val kotlin = "1.8.0"
    const val appCompat = "1.7.0-alpha02"
    const val constraintLayout = "2.2.0-alpha07"
    const val ktx = "1.8.0"
    const val material = "1.9.0-alpha01"
    const val hilt_compiler = "1.1.0"

    //Version codes for all the test libraries
    const val junit4 = "4.13.2"
    const val testRunner = "1.5.2"
    const val espresso = "3.5.1"
    const val annotation = "1.6.0-rc01"
    const val kotlinSerializable = "1.4.1"


    // Converters
    const val gson = "2.6.2"
    const val googleGson = "2.9.0"

    // Network
    const val retrofit = "2.11.0"
    const val retrofitMock = "4.12.0"
    const val converter = "0.8.0"
    const val okhttpVersion = "5.0.0-alpha.6"

    // work manager
    const val work = "2.7.0"

    // Mockk
    const val mockk = "1.13.11"

    // Gradle Plugins
    const val ktlint = "11.1.0"
    const val detekt = "1.22.0"
    const val spotless = "6.14.1"
    const val dokka = "1.7.20"
    const val gradleVersionsPlugin = "0.45.0"
}

object BuildPlugins {
    //All the build plugins are added here
    const val androidLibrary = "com.android.library"
    const val ktlintPlugin = "org.jlleitschuh.gradle.ktlint"
    const val detektPlugin = "io.gitlab.arturbosch.detekt"
    const val spotlessPlugin = "com.diffplug.spotless"
    const val dokkaPlugin = "org.jetbrains.dokka"
    const val androidApplication = "com.android.application"
    const val kotlinAndroid = "org.jetbrains.kotlin.android"
    const val kotlinParcelizePlugin = "org.jetbrains.kotlin.plugin.parcelize"
    const val gradleVersionsPlugin = "com.github.ben-manes.versions"
}

object Libraries {
    //Any Library is added here
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val constraintLayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val materialComponents = "com.google.android.material:material:${Versions.material}"
    // Conversion
    const val retrofitGsonConverter = "com.squareup.retrofit2:converter-gson:${Versions.gson}"
    const val googleGson = "com.google.code.gson:gson:${Versions.googleGson}"

    // Network
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val converter = "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:${Versions.converter}"

    // Serializable
    const val kotlinSerializable = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinSerializable}"

    // Work manager
    const val work = "androidx.work:work-runtime-ktx:${Versions.work}"
    const val hiltWork = "androidx.hilt:hilt-work:${Versions.hilt_compiler}"
}

object TestLibraries {
    //any test library is added here
    const val junit4 = "junit:junit:${Versions.junit4}"
    const val testRunner = "androidx.test:runner:${Versions.testRunner}"
    const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val annotation = "androidx.annotation:annotation:${Versions.annotation}"
    const val mockWebServer = "com.squareup.okhttp3:mockwebserver:${Versions.retrofitMock}"

}


object AndroidSdk {
    const val minSdkVersion = 21
    const val compileSdkVersion = 35
    const val targetSdkVersion = compileSdkVersion
    const val versionCode = 1
    const val versionName = "1.0"
}