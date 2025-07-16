plugins {
    id(BuildPlugins.androidApplication)
    id(BuildPlugins.kotlinAndroid)
    id(BuildPlugins.kotlinParcelizePlugin)
    id(BuildPlugins.ktlintPlugin)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {

    compileSdk = AndroidSdk.compileSdkVersion
    android.buildFeatures.dataBinding = true
    android.buildFeatures.viewBinding = true

    defaultConfig {
        applicationId = "keronei.swapper"
        minSdk = AndroidSdk.minSdkVersion
        targetSdk = AndroidSdk.targetSdkVersion
        versionCode = AndroidSdk.versionCode
        versionName = AndroidSdk.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8" // Or '11'
    }

    testOptions {
        animationsDisabled = true
        unitTests.apply {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    namespace = "keronei.swapper"
    buildFeatures {
        viewBinding = true
    }

    dependencies {
        implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
        implementation(Libraries.appCompat)
        implementation(Libraries.constraintLayout)
        implementation(Libraries.materialComponents)
        implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
        implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")
        implementation ("com.google.android.gms:play-services-location:21.3.0")
        implementation ("com.vmadalin:easypermissions-ktx:1.0.0")
        implementation("androidx.legacy:legacy-support-v4:1.0.0")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.1")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
        implementation("androidx.fragment:fragment-ktx:1.8.8")
        implementation("com.google.android.material:material:1.12.0")
        implementation("androidx.recyclerview:recyclerview:1.4.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
        implementation("com.google.dagger:hilt-android:2.56.2")
        kapt("com.google.dagger:hilt-android-compiler:2.55")
        implementation("com.github.bumptech.glide:glide:4.16.0")
        annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
        implementation("androidx.camera:camera-camera2:1.4.2")
        implementation("androidx.camera:camera-core:1.4.2")
        implementation("androidx.camera:camera-lifecycle:1.4.2")
        implementation("androidx.camera:camera-view:1.4.2")

        androidTestImplementation(TestLibraries.testRunner)
        androidTestImplementation(TestLibraries.espresso)
        androidTestImplementation(TestLibraries.annotation)
        testImplementation(TestLibraries.junit4)
    }
}


