plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.watchmymoney"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.watchmymoney"
        minSdk = 37 // 僅支援 Wear OS 7.0 (Android 17)
        targetSdk = 37
        versionCode = 6
        versionName = "2.0.0-wearos7"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    lint {
        abortOnError = true
        textReport = true
        xmlReport = true
        htmlReport = true
    }
}

val isTestExecution = gradle.startParameter.taskNames.any { it.contains("test", ignoreCase = true) }

tasks.matching { it.name.contains("AarMetadata") }.configureEach {
    enabled = false
}

tasks.matching { it.name == "processDebugManifest" }.configureEach {
    inputs.property("isTestExecution", isTestExecution)
    doLast {
        if (isTestExecution) {
            val manifestFile = layout.buildDirectory.file("intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml").get().asFile
            if (manifestFile.exists()) {
                val content = manifestFile.readText()
                manifestFile.writeText(content.replace("android:minSdkVersion=\"37\"", "android:minSdkVersion=\"34\""))
            }
        }
    }
}

tasks.matching { it.name == "processDebugManifestForPackage" }.configureEach {
    inputs.property("isTestExecution", isTestExecution)
    doLast {
        if (isTestExecution) {
            val manifestFile = layout.buildDirectory.file("intermediates/packaged_manifests/debug/processDebugManifestForPackage/AndroidManifest.xml").get().asFile
            if (manifestFile.exists()) {
                val content = manifestFile.readText()
                manifestFile.writeText(content.replace("android:minSdkVersion=\"37\"", "android:minSdkVersion=\"34\""))
            }
        }
    }
}

tasks.matching { it.name == "processDebugResources" }.configureEach {
    inputs.property("isTestExecution", isTestExecution)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    
    // Compose for Wear OS Material 3
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation)
    
    // Lifecycle & ViewModel Compose
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Complications
    implementation(libs.androidx.watchface.complications.data.source.ktx)
    
    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)
    
    // Material Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Unit & Robolectric Testing
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
