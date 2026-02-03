package com.example.gradebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gradebook.ui.auth.LoginScreen
import com.example.gradebook.ui.auth.RegisterScreen
import com.example.gradebook.ui.home.HomeScreen
import com.example.gradebook.ui.theme.GradeBookTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GradeBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GradeBookApp()
                }
            }
        }
    }
}

@Composable
fun GradeBookApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { teacherId ->
                    navController.navigate("home/$teacherId") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { teacherId ->
                    navController.navigate("home/$teacherId") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "home/{teacherId}",
            arguments = listOf(navArgument("teacherId") { type = NavType.IntType })
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: 0
            HomeScreen(
                teacherId = teacherId,
                onClassClick = { classId -> navController.navigate("class_detail/$classId") }
            )
        }
        composable(
            route = "class_detail/{classId}",
            arguments = listOf(navArgument("classId") { type = NavType.IntType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            com.example.gradebook.ui.classdetail.ClassDetailScreen(
                classroomId = classId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAttendance = { navController.navigate("attendance/$classId") },
                onNavigateToGrades = { navController.navigate("grades/$classId") }
            )
        }
        composable(
            route = "attendance/{classId}",
            arguments = listOf(navArgument("classId") { type = NavType.IntType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            com.example.gradebook.ui.attendance.AttendanceScreen(
                classroomId = classId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "grades/{classId}",
            arguments = listOf(navArgument("classId") { type = NavType.IntType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            com.example.gradebook.ui.grades.GradeEntryScreen(
                classroomId = classId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}