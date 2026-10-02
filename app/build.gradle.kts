plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
//    alias(libs.plugins.google.services)
}

android {
    namespace = "com.daumo.dynamicis"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.daumo.dynamicis"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Compose
    implementation(platform(libs.androidx.compose.bom.v20251001))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    // Override BOM version to get the below alpha version of Material3 components (used by: ModalBottomSheet)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.runtime.livedata)

    // Additional Compose dependencies
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.navigation.compose.v295)
    implementation(libs.androidx.media3.session)

    // Firebase
    implementation(platform(libs.firebase.bom.v3440))
//    implementation("com.google.firebase:firebase-auth-ktx")
//    implementation("com.google.firebase:firebase-firestore-ktx")

    // Accompanist
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.placeholder.material)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.accompanist.webview)
    implementation(libs.accompanist.navigation.animation)

    // Landscapist
    implementation(libs.landscapist.glide)
    implementation(libs.landscapist.palette)
    implementation(libs.landscapist.animation)

    // Other
    implementation(libs.androidx.core.splashscreen.v101) // Splash screen
    implementation(libs.coil.compose) // Image loading
    implementation(libs.gson) // Serialization / Deserialization
    implementation(libs.lottie.compose) // Animations
    implementation(libs.composewaveloading) // Wave animation: https://github.com/vitaviva/ComposeWaveLoading

    // Compose Tests
    androidTestImplementation(platform(libs.androidx.compose.bom.v20260900))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.core.ktx.v1191)
    implementation(libs.androidx.lifecycle.runtime.ktx.v294)
    implementation(libs.androidx.activity.compose.v1110)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //implementation("com.google.android.gms:play-services-auth:20.5.0")
}
