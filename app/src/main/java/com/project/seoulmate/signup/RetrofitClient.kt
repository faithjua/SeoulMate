package com.project.seoulmate.signup

//package com.project.seoulmate.network
import com.project.seoulmate.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor
//import androidx.privacysandbox.tools.core.generator.build
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
//import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.OkHttpClient
import retrofit2.converter.kotlinx.serialization.asConverterFactory


object RetrofitClient {
    //애뮬레이터에서 내 서버에 접속하기 위한 URL
    private const val BASE_URL = BuildConfig.BASE_URL

    // 1. 로깅 인터셉터 설정 (OkHttp3 라이브러리 필요)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2. OkHttpClient 설정 (인터셉터 연결)
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val authApi: AuthApi by lazy {
        val json = Json {
            ignoreUnknownKeys = true // 서버에서 오는 모르는 키는 무시
            coerceInputValues = true
        }
        Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            // Gson 대신 kotlinx-serialization 공식 컨버터 장착
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApi::class.java)
    }
}