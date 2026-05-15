plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.memegenerator1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.memegenerator1"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // implementation("androidx.core:core-ktx:1.16.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("io.coil-kt:coil-compose:2.5.0")  // coil for loading images
    implementation("com.squareup.retrofit2:retrofit:2.9.0")  // retrofit for making network calls
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Material 3 Compose
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("com.google.android.material:material:1.6.1")
    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.graphics:graphics-core:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    // Unit testing dependency
    testImplementation("junit:junit:4.13.2")
}