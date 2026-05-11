package com.project.seoulmate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.project.seoulmate.signup.LoginState
import com.project.seoulmate.signup.LoginViewModel
import com.project.seoulmate.ui.screens.addmeeting.AddMeetingScreen
import com.project.seoulmate.ui.screens.home.HomeScreen
import com.project.seoulmate.ui.screens.notification.NotificationScreen

import com.project.seoulmate.signup.LoginScreen
import com.project.seoulmate.signup.SignupScreen
//import com.project.seoulmate.signup.CourseAddScreen
//import com.project.seoulmate.signup.CourseAddViewModel
import com.project.seoulmate.ui.screens.profile.ProfileScreen
import com.project.seoulmate.ui.screens.meeting.MeetingDetailScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel


/**
 * 앱의 화면 이동 경로(Navigation Graph)를 정의
 */

/**
 * 앱의 모든 화면 경로(route)를 sealed class로 정의
 * 오타로 인한 버그를 방지하기 위해 문자열 대신 타입 안전한 방식을 사용합니다.
 */
 
sealed class Screen(val route: String) {
    /** 로그인 및 찜화면 추가 **/
    object Login : Screen("login")
    object Signup : Screen("signup")

    /** 홈 화면 */
    object Home : Screen("home")
    /** 만남 등록/수정 화면 */
    object AddMeeting : Screen("add_meeting?meetingId={meetingId}") {
        fun createRoute(meetingId: String? = null): String {
            return if (meetingId != null) {
                "add_meeting?meetingId=$meetingId"
            } else {
                "add_meeting"
            }
        }
    }
    /** 알림 화면 */
    object Notifications : Screen("notifications")
    /** 검색 화면 */
    object Search : Screen("search")
    /** 찜 화면 */
    object Wishlist : Screen("wishlist")


    /** 코스 추가 화면 */
    object AddCourse : Screen("add_course")
    /** 프로필 화면 */
    object Profile : Screen("profile")
    /** 만남 상세 화면 */
    object MeetingDetail : Screen("meeting_detail/{meetingId}") {
        fun createRoute(meetingId: String) = "meeting_detail/$meetingId"
    }
}

/**
 * NavHost: 화면들을 담는 컨테이너.
 * startDestination: 앱 시작 시 첫 화면
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {

    
    // 1. 로그인 상태 관찰
    val loginState by loginViewModel.loginState.collectAsState()

    /*
    // 2. 상태 변화에 따른 자동 네비게이션 처리 (LaunchedEffect)
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                if (state.member.isNewMember ?: true) {
                    // 신규 회원이면 회원가입 화면으로 이동
                    navController.navigate(Screen.Signup.route) {
                        // 로그인 화면을 백스택에서 제거 (뒤로가기 방지)
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    // 기존 회원이면 홈 화면으로 이동
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            else -> { /* Error나 Loading 상태는 각 화면에서 처리 */ }
        }
    }

     */

    // 3. 네비게이션 그래프 정의
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // --- 인증 관련 화면 ---
        composable(route = Screen.Login.route) {
            //별도로 넘겨주지 않아도 LoginScreen 내부에서 hiltViewModel()을 호출하면 동일한 인스턴스를 참조하게 할 수 있습니다.
            LoginScreen(
                viewModel = loginViewModel,
                // 네비게이션 동작을 통째로 LoginScreen에 콜백으로 넘겨줍니다!
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Signup.route) {
            SignupScreen(
                idToken = loginViewModel.currentIdToken,
                email = loginViewModel.currentUserEmail,
                onSignupSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onCancelSignup = {                                          // 가입 취소
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }


        // 홈 화면
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        // 만남 등록/수정 화면
        composable(
            route = Screen.AddMeeting.route,
            arguments = listOf(
                navArgument("meetingId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
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


        // 코스 추가 화면
        composable(route = Screen.AddCourse.route) {
            com.project.seoulmate.ui.screens.addcourse.AddCourseScreen(navController = navController)
        }
        // 프로필 화면
        composable(route = Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        // 만남 상세 화면
        composable(
            route = Screen.MeetingDetail.route,
            arguments = listOf(navArgument("meetingId") { type = NavType.StringType })
        ) {
            MeetingDetailScreen(navController = navController)

        }
    }
}
