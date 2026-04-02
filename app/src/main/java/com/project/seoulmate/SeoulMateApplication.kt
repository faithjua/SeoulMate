package com.project.seoulmate

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Hilt를 사용하기 위해 필요한 Application 클래스
 */
@HiltAndroidApp
class SeoulMateApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        // 디버그 모드일 때만 Timber로 로그를 볼 수 있게 함
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
