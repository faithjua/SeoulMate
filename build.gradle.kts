// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false // 2.0.21로 통일
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false // Kotlin 버전과 맞춤

    // 로그인과 Retrofit을 위해 필요한 플러그인
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false // Kotlin 버전과 맞춤

    // DI를 위한 플러그인
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false // Kotlin 2.0.21용 KSP 버전
    id("com.google.dagger.hilt.android") version "2.52" apply false
}