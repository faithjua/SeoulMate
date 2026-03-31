/*package com.project.seoulmate.signup

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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.UUID
import okhttp3.Interceptor


object RetrofitClient {
    //애뮬레이터에서 내 서버에 접속하기 위한 URL
    private const val BASE_URL = BuildConfig.BASE_URL

    // 1. 로깅 인터셉터 설정 (OkHttp3 라이브러리 필요)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2. 로그 모니터링을 위해 추가하는 Trace ID 인터셉터 (헤더 주입용) ️
    private val traceInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // 기존 요청을 복사하면서 헤더를 하나 끼워 넣습니다.
        val newRequest = originalRequest.newBuilder()
            .header("X-Trace-Id", UUID.randomUUID().toString()) // 매번 새로운 UUID 생성
            .build()

        // 변형된 새 요청을 서버로 보냅니다.
        chain.proceed(newRequest)
    }
    // 2. OkHttpClient 설정 (인터셉터 연결)
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging) //바디로그 찍는 용도
        .addInterceptor( traceInterceptor ) //헤더에 ID 달아줌
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    //  1. 공통 붕어빵 기계(Retrofit)를 하나 딱 만들어 둡니다.
    private val retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    //  2. 기존 Auth API 뽑아내기
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    //  3. 우리가 쓸 AI API 쏙 뽑아내기! (이게 있어야 뷰모델에서 씁니다)
    val aiApi: AiApi by lazy {
        retrofit.create(AiApi::class.java)
    }



}
 */
