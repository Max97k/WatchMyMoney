plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.watchmymoney"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.watchmymoney"
        minSdk = 30 // Wear OS 3.0+
        targetSdk = 34 // Android 14 (Wear OS 5 target)
        versionCode = 3
        versionName = "1.1"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            debugSymbolLevel = "FULL"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.wear)
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)
    
    // Complications
    implementation(libs.androidx.watchface.complications.data.source.ktx)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.wear.input)
    implementation(libs.androidx.compose.material.icons.extended)
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
