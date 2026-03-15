import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //새로 추가 부분
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.serialization")
    //2.2.0으로 업글 후 호환문제 발생
    id("org.jetbrains.kotlin.plugin.compose")
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
            properties.getProperty("BASE_URL") ?: "\"http://localhost:8080\""

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))


    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")


    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation("androidx.credentials:credentials:1.2.0-rc01")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.0-rc01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")

//  변경된 네트워크 통신 & 직렬화 라이브러리
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // 2.11.0 최신 안정 버전으로 업그레이드!
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // Kotlin 1.9.0과 찰떡인 버전
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0") // 공식 직렬화 컨버터
    implementation("com.squareup.okhttp3:okhttp:5.3.0")
    //   Compose에서 ViewModel을 사용하기 위해 반드시 필요
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    //   Lifecycle 관련 Compose 확장
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    //네트워크 로그 확인용
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
