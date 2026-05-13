package com.project.seoulmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.project.seoulmate.ui.navigation.AppNavGraph
import com.project.seoulmate.ui.theme.SeoulMateTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 앱의 진입점.
 *
 * AppCompatActivity 를 상속해야 AppCompatDelegate.setApplicationLocales 가
 * API 32 이하에서도 자동으로 액티비티를 재생성한다.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeoulMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // NavController: 화면 이동 기록(백스택)을 관리하는 컨트롤러
                    val navController = rememberNavController()
                    // AppNavGraph: 모든 화면과 이동 경로가 정의된 네비게이션 그래프
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}