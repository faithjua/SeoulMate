package com.project.seoulmate.di

import com.project.seoulmate.BuildConfig
import com.project.seoulmate.data.remote.NaverSearchApi
import com.project.seoulmate.signup.CourseApi
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
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import timber.log.Timber

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NaverRetrofit

@Module
@InstallIn(SingletonComponent::class) // 앱 전체에서 하나만 유지됨 (싱글톤)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = false // null이나 기본값 필드는 JSON에서 제외하여 서버 부담을 줄임
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor{ message ->
            Timber.tag("OkHttp_SeoulMate").d(message)
        }.apply {
            //level = HttpLoggingInterceptor.Level.BODY
            // BuildConfig.DEBUG를 사용하여 디버그 모드일 때만 BODY 로그를 찍고,
            // 출시용(Release) 앱에서는 로그를 아예 찍지 않거나 최소화합니다.
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            // 특정 헤더(Authorization)만 로그에서 가림
            redactHeader("Authorization")
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
            // ------------------------------- 첫 로그인시 시간이 오래 걸림
            .connectTimeout(30, TimeUnit.SECONDS) // 서버 연결 시도 제한 시간
            .readTimeout(30, TimeUnit.SECONDS)    // 서버로부터 응답 데이터를 읽는 제한 시간
            .writeTimeout(30, TimeUnit.SECONDS)   // 서버로 데이터를 보내는 제한 시간
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BuildConfig.BASE_URL) // local.properties에 정의한 BASE_URL
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
    fun provideCourseApi(retrofit: Retrofit): CourseApi {
        return retrofit.create(CourseApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMeetingApi(retrofit: Retrofit): com.project.seoulmate.data.remote.MeetingApi {
        return retrofit.create(com.project.seoulmate.data.remote.MeetingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFavoriteApi(retrofit: Retrofit): com.project.seoulmate.data.remote.FavoriteApi {
        return retrofit.create(com.project.seoulmate.data.remote.FavoriteApi::class.java)
    }

    // --- 네이버 API용 Retrofit (별도 Base URL) ---

    @Provides
    @Singleton
    @NaverRetrofit
    fun provideNaverRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://openapi.naver.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideNaverSearchApi(@NaverRetrofit retrofit: Retrofit): NaverSearchApi {
        return retrofit.create(NaverSearchApi::class.java)
    }
}