package com.project.seoulmate.di

import com.project.seoulmate.BuildConfig
import com.project.seoulmate.signup.AiApi
import com.project.seoulmate.signup.AuthApi // 기존 AuthApi 위치
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // 앱 전체에서 하나만 유지됨 (싱글톤)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val traceInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("X-Trace-Id", UUID.randomUUID().toString())
                .build()
            chain.proceed(newRequest)
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(traceInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // --- 여기서부터 실제 API 인스턴스들을 제공합니다 ---

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAiApi(retrofit: Retrofit): AiApi {
        return retrofit.create(AiApi::class.java)
    }
}