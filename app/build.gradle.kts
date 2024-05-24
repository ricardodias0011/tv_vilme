plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    signingConfigs {
        create("release") {
        }
    }
    namespace = "com.nest.nestplay"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nest.nestplay"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        signingConfig = signingConfigs.getByName("release")

    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
        dataBinding = true
        viewBinding = true
    }

    viewBinding {
        enable = true
    }
}

dependencies {
    implementation("com.airbnb.android:lottie:6.4.0")

    implementation("androidx.leanback:leanback:1.1.0-rc02")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.github.bumptech.glide:glide:4.11.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.28")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

    implementation("com.google.android.exoplayer:exoplayer:2.19.1")

    implementation("com.google.code.gson:gson:2.8.8")

    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:2.9.9")
}