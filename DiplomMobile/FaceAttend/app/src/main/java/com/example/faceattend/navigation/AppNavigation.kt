package com.example.faceattend.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.faceattend.data.TokenManager
import com.example.faceattend.data.UserProfileManager
import com.example.faceattend.screens.AlertScreen
import com.example.faceattend.screens.AuthScreen
import com.example.faceattend.screens.CourseAttendanceScreen
import com.example.faceattend.screens.CourseDetailsScreen
import com.example.faceattend.screens.CoursesScreen
import com.example.faceattend.screens.LectureDetailsScreen
import com.example.faceattend.screens.adminScreens.CoursesAdminScreen
import com.example.faceattend.screens.adminScreens.GroupScreen
import com.example.faceattend.screens.adminScreens.StudentsScreen
import com.example.faceattend.screens.adminScreens.SubjectsScreen
import com.example.faceattend.screens.home.AdminHomeScreen
import com.example.faceattend.screens.home.LectorHomeScreen
import com.example.faceattend.screens.home.StudentHomeScreen
import com.example.faceattend.utils.SessionExpiredManager
import com.example.faceattend.views.AuthViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


@Composable
fun AppNavigation(navController : NavHostController) {
    val tokenManager: TokenManager = koinInject()
    val userManager : UserProfileManager = koinInject()
    val sessionExpired by SessionExpiredManager.showDialog.collectAsState()
    val startDestination = if (tokenManager.isTokenValid()) {
        Log.d("AppNavigation", "Token valid, start = HOME")
        Routes.Home.route
    } else {
        Log.d("AppNavigation", "Token invalid, start = AUTH")
        tokenManager.clear()
        userManager.clear()
        Routes.Auth.route
    }

    if (sessionExpired) {
        AlertScreen(
            onTokenExpiration = {
                SessionExpiredManager.reset()
                tokenManager.clear()
                userManager.clear()
                navController.navigate(Routes.Auth.route) {
                    popUpTo(Routes.Home.route) { inclusive = true }
                }
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Auth.route) {
            val authViewModel: AuthViewModel = koinViewModel()
            AuthScreen(
                authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.Auth.route) {
                        popUpTo(Routes.Auth.route) { inclusive = true }
                    }
                })
        }
        composable(Routes.Home.route) {
            val role = tokenManager.getRole()
            when (role?.lowercase()) {
                "student" -> StudentHomeScreen(
                    onEditProfile = { /* TODO */ },
                    onLogout = {
                        tokenManager.clear()
                        userManager.clear()
                        navController.navigate(Routes.Auth.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    },
                    onStudentCourses = { navController.navigate(Routes.Courses.route) }
                )
                "lector" -> LectorHomeScreen(
                    onEditProfile = { /* TODO */ },
                    onLogout = {
                        tokenManager.clear()
                        userManager.clear()
                        navController.navigate(Routes.Auth.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    },
                    onLecturerCourses = { navController.navigate(Routes.Courses.route) }
                )
                "admin" -> AdminHomeScreen(
                    onEditProfile = { /* TODO */ },
                    onLogout = {
                        userManager.clear()
                        tokenManager.clear()
                        navController.navigate(Routes.Auth.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    },
                    onAdminGroups = { navController.navigate(Routes.Group.route) },
                    onAdminCourses = { navController.navigate(Routes.Courses.route) },
                    onAdminStudents = { navController.navigate(Routes.Students.route) },
                    onAdminSubjects = { navController.navigate(Routes.Subject.route)  }
                )
                else -> {

                    LaunchedEffect(Unit) {
                        tokenManager.clear()
                        navController.navigate(Routes.Auth.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    }
                }
            }
        }
        composable(Routes.Courses.route) { backStackEntry ->
            val role = tokenManager.getRole()
            when (role?.lowercase()) {
                "admin" -> CoursesAdminScreen (
                    onBack = {
                        if (navController.currentDestination?.route == Routes.Courses.route) {
                            navController.popBackStack()
                        }},
                )
                else -> {
                    CoursesScreen(
                        onBack = {
                            if (navController.currentDestination?.route == Routes.Courses.route) {
                                navController.popBackStack()
                            }},
                        onCourseClick = {
                                courseId ->
                            navController.navigate("${Routes.Course.route}/$courseId")
                        }
                    )
                }
            }
        }
        composable(Routes.CourseWithId.route) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")?.toIntOrNull() ?: 0
            CourseDetailsScreen(
                courseId = courseId,
                onBack = { if (navController.currentDestination?.route == Routes.CourseWithId.route) {
                    navController.popBackStack()
                }},
                onLectureClick = { lectureId ->
                    navController.navigate("lecture/$lectureId")
                },
                onAttendanceClick = {
                    navController.navigate("attendance/$courseId")
                }
            )
        }
        composable(Routes.Lecture.route) { backStackEntry ->
            val lectureId = backStackEntry.arguments?.getString("lectureId")?.toIntOrNull() ?: 0
            LectureDetailsScreen(
                lectureId = lectureId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Attendance.route) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")?.toIntOrNull() ?: 0
            CourseAttendanceScreen(
                courseId = courseId,
            ) { navController.popBackStack() }
        }
        composable(Routes.Subject.route) {
            SubjectsScreen(
                onBack = {navController.popBackStack()}
            )
        }
        composable(Routes.Group.route) {
            GroupScreen(
                onBack = {navController.popBackStack()}
            )
        }
        composable(Routes.Students.route) {
            StudentsScreen(
                onBack = {navController.popBackStack()}
            )
        }
        composable(Routes.Camera.route) { backStackEntry ->
            val lectureId = backStackEntry.arguments?.getString("lectureId")?.toIntOrNull() ?: 0
            // Здесь будет AttendanceScreen
        }
    }
}

