package com.ironmind.here

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ironmind.here.data.DatabaseHelper
import com.ironmind.here.ui.*
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DatabaseHelper.UpdateLocal(this)
        setContent {
            CustomAppTheme {
                HereApp()
            }
        }
    }
}

@Composable
fun CustomAppTheme(content: @Composable () -> Unit) {
    val colors = lightColors(
        primary = Color(0xFF2E7D32),
        primaryVariant = Color(0xFF1B5E20),
        secondary = Color(0xFF66BB6A),
        background = Color(0xFFF6FFF8),
        surface = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black
    )

    MaterialTheme(
        colors = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

@Composable
fun HereApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: ""

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onReady = { navController.navigate("login") },
                onError = { }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { userId, role, displayName ->
                    navController.navigate("main/$userId/$role/$displayName")
                }
            )
        }

        composable(
            "main/{userId}/{role}/{displayName}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType },
                navArgument("displayName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: ""
            val displayName = backStackEntry.arguments?.getString("displayName") ?: ""

            val subNavController = rememberNavController()
            val subRoute by subNavController.currentBackStackEntryAsState()
            val currentSubRoute = subRoute?.destination?.route ?: "home"

            MainScaffold(
                userId = userId,
                etudiantName = displayName,
                currentRoute = currentSubRoute,
                onNavigateToHome = { subNavController.navigate("home") },
                onNavigateToSchedule = { subNavController.navigate("schedule") },
                onNavigateToProfile = { subNavController.navigate("profile") }
            ) {
                NavHost(
                    navController = subNavController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(userId = userId)
                    }
                    composable("schedule") {
                        ScheduleScreen(userId = userId, role = role, navController = navController)
                    }
                    composable("profile") {
                        ProfileScreen(
                            userId = userId,
                            role = role,
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
