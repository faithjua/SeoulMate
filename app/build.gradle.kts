import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //새로 추가 부분
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.serialization")
    //2.2.0으로 업글 후 호환문제 발생
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

val properties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { properties.load(it) }
}

android {
    namespace = "com.project.seoulmate"
    compileSdk = 34

    defaultConfig {
        buildConfigField(
            "String",
            "BASE_URL",
            "\"${properties.getProperty("BASE_URL") ?: "http://localhost:8080"}\""
            //properties.getProperty("BASE_URL") ?: "\"http://localhost:8080\""

        )
        // 네이버 API 키
        buildConfigField(
            "String",
            "NAVER_CLIENT_ID",
            "\"${properties.getProperty("naver.client.id") ?: ""}\""
        )
        buildConfigField(
            "String",
            "NAVER_CLIENT_SECRET",
            "\"${properties.getProperty("naver.client.secret") ?: ""}\""
        )
        applicationId = "com.project.seoulmate"
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    //implementation("androidx.core:core-ktx:1.12.0")
    //implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")

    // 1. Compose BOM 및 UI
    //implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2") // activity-compose 버전 업
    implementation("androidx.compose.material:material-icons-extended")

    // 2. Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-android-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // 3. Firebase & Login
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation("com.google.firebase:firebase-auth")
    //implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // 4. Network & Serialization
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // okhttp 버전 4.12.0 권장
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 5. Navigation & Lifecycle
    implementation("androidx.navigation:navigation-compose:2.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")

    // 테스트용 라이브러리
    testImplementation("junit:junit:4.13.2")
    //androidTestImplementation("androidx.test.ext:junit:1.2.1")
    //androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    //androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.03"))
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling") // Preview 기능을 위해 필수
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // 네이버 지도 SDK
    implementation("com.naver.maps:map-sdk:3.23.1")
    // Compose용 네이버 지도 라이브러리
    implementation("io.github.fornewid:naver-map-compose:1.5.7")

    //로그를 위한 timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // 이미지 로딩 Coil
    implementation("io.coil-kt:coil-compose:2.6.0")
}

