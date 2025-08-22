plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.dive_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dive_app"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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

//    composeOptions {
//        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
//    }
}

dependencies {
    // Compose BOM으로 버전 정리
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    // 기본 Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.animation:animation")

    // Accompanist
    implementation("com.google.accompanist:accompanist-navigation-animation:0.34.0")

    // ★ Wear Compose (로터리 이벤트 onRotaryScrollEvent)
    implementation("androidx.wear.compose:compose-foundation:1.3.1")
    implementation("androidx.wear.compose:compose-material:1.3.1")

    // 버전 카탈로그 사용 항목 (libs.*) — 중복되지 않는 것만 유지
    implementation(libs.activity.compose)
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.play.services.wearable)
    implementation(libs.navigation.compose)
    implementation(libs.material3.android)
    implementation(libs.material.icons.extended)

    implementation("com.naver.maps:map-sdk:3.22.1")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")

    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.material:material-icons-extended:<compose_version>")
    // 테스트/디버그
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    implementation("androidx.wear.compose:compose-navigation:1.3.1")
// 최신으로 맞추세요
    implementation("androidx.wear.compose:compose-material:1.3.1")
}

