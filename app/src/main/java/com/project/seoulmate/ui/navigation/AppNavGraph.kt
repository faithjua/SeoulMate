package com.project.seoulmate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.project.seoulmate.ui.screens.addmeeting.AddMeetingScreen
import com.project.seoulmate.ui.screens.home.HomeScreen
import com.project.seoulmate.ui.screens.notification.NotificationScreen

/**
 * 앱의 화면 이동 경로(Navigation Graph)를 정의
 */

/**
 * 앱의 모든 화면 경로(route)를 sealed class로 정의
 * 오타로 인한 버그를 방지하기 위해 문자열 대신 타입 안전한 방식을 사용합니다.
 */
 
sealed class Screen(val route: String) {
    /** 홈 화면 */
    object Home : Screen("home")
    /** 만남 등록 화면 */
    object AddMeeting : Screen("add_meeting")
    /** 알림 화면 */
    object Notifications : Screen("notifications")
    /** 검색 화면 */
    object Search : Screen("search")
    /** 찜 화면 */
    object Wishlist : Screen("wishlist")
}

/**
 * NavHost: 화면들을 담는 컨테이너.
 * startDestination: 앱 시작 시 첫 화면
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // 홈 화면
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        // 만남 등록 화면
        composable(route = Screen.AddMeeting.route) {
            AddMeetingScreen(navController = navController)
        }
        // 알림 화면
        composable(route = Screen.Notifications.route) {
            NotificationScreen(navController = navController)
        }
        // 검색 화면
        composable(route = Screen.Search.route) {
            com.project.seoulmate.ui.screens.search.SearchScreen(navController = navController)
        }
        // 찜 화면
        composable(route = Screen.Wishlist.route) {
            com.project.seoulmate.ui.screens.wishlist.WishlistScreen(navController = navController)
        }
    }
}
