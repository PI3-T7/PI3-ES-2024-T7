plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.secrets_gradle_plugin") version "0.5"
}

android {
    namespace = "br.edu.puccampinas.projeto_smart_locker"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.edu.puccampinas.projeto_smart_locker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // API gerador de QRcode
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // API QRcode Scanner - https://github.com/yuriy-budiyev/code-scanner
    implementation("com.github.yuriy-budiyev:code-scanner:2.3.0")

    // https://github.com/santalu/maskara - biblioteca mascaras
    implementation("com.github.santalu:maskara:1.0.0")

    // Configuração do Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    // Configuração do Authentication
    implementation("com.google.firebase:firebase-auth")
    // Configuração do Firestore Database
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Configuração do Firebase Storage
    implementation("com.google.firebase:firebase-storage:21.0.0")

    // Biblioteca LottieAnimation
    implementation("com.airbnb.android:lottie:6.0.0")

    // https://github.com/ybq/Android-SpinKit - Biblioteca de animações de loading
    implementation("com.github.ybq:Android-SpinKit:1.4.0")

    implementation ("com.google.maps:google-maps-services:0.18.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.0")
    implementation("androidx.activity:activity:1.8.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("com.google.guava:guava:31.0.1-android")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation ("com.google.android.gms:play-services-cronet:18.0.1")
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-core:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")

}