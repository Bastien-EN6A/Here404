package com.ironmind.here

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.ironmind.here.data.DatabaseHelper
import com.ironmind.here.ui.*
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread {
            DatabaseHelper.UpdateLocal(this)  //on met a jour la base de donnée locale
        }.start()
        setContent {
            HereApp()
        }
    }
}

@Composable
fun HereApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onReady = { navController.navigate("login") },
                onError = { /* gérer les erreurs */ }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { userId, role, displayName ->
                    navController.navigate("home/$userId/$role/$displayName")
                }
            )
        }

        composable(
            "home/{userId}/{role}/{displayName}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType },
                navArgument("displayName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: ""
            val displayName = backStackEntry.arguments?.getString("displayName") ?: ""

            MainScaffold(
                userId = userId,
                etudiantName = displayName,
                onNavigateToHome = {
                    navController.navigate("home/$userId/$role/$displayName")
                },
                onNavigateToSchedule = {
                    navController.navigate("schedule/$userId/$role/$displayName")
                }
            ) {
                HomeScreen(userId = userId)
            }
        }

        composable(
            "schedule/{userId}/{role}/{displayName}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType },
                navArgument("displayName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: ""
            val displayName = backStackEntry.arguments?.getString("displayName") ?: ""

            MainScaffold(
                userId = userId,
                etudiantName = displayName,
                onNavigateToHome = {
                    navController.navigate("home/$userId/$role/$displayName")
                },
                onNavigateToSchedule = {
                    navController.navigate("schedule/$userId/$role/$displayName")
                }
            ) {
                ScheduleScreen(
                    userId = userId,
                    role = role,
                    navController = navController
                )
            }
        }

        // ✅ Passage du navController à PresenceScreen
        composable(
            "presence/{seanceId}/{groupe}",
            arguments = listOf(
                navArgument("seanceId") { type = NavType.IntType },
                navArgument("groupe") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val seanceId = backStackEntry.arguments?.getInt("seanceId") ?: 0
            val groupe = backStackEntry.arguments?.getString("groupe") ?: ""
            PresenceScreen(navController = navController, seanceId = seanceId, groupe = groupe)
        }
    }
}
