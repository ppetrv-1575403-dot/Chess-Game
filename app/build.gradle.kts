plugins {
    alias(libs.plugins.android.application)
    //alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.compose)
    //alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.p_soft.chess"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.p_soft.chess"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.compose.ui.tooling)
    //implementation(libs.compose.ui)
    //implementation(libs.compose.material3)
    //implementation(libs.compose.material.icons)
    //implementation(libs.androidx.compose.material.icons.extended)

    //compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
    //compose-ui = { group = "androidx.compose.ui", name = "ui" }
    //compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
    //compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
    //compose-material3 = { group = "androidx.compose.material3", name = "material3" }
    //compose-material-icons

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Core
    implementation(libs.core.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.tooling)
}

/*
kapt {
    correctErrorTypes = true
}*/
