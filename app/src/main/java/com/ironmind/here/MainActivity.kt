package com.ironmind.here

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.ironmind.here.data.DatabaseHelper
import com.ironmind.here.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                onLoginSuccess = { userId ->
                    navController.navigate("home/$userId")
                }
            )
        }

        composable(
            "home/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val context = LocalContext.current
            var etudiantName by remember { mutableStateOf("") }

            LaunchedEffect(userId) {
                if (userId.isNotEmpty()) {
                    val (nom, prenom) = withContext(Dispatchers.IO) {
                        DatabaseHelper.getEtudiantById(context, userId)
                    }
                    etudiantName = "$prenom $nom"
                }
            }

            MainScaffold(
                userId = userId,
                etudiantName = etudiantName,
                onNavigateToHome = { navController.navigate("home/$userId") },
                onNavigateToSchedule = { navController.navigate("schedule/$userId") }
            ) {
                HomeScreen(userId = userId)
            }
        }

        composable(
            "schedule/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val context = LocalContext.current
            var etudiantName by remember { mutableStateOf("") }

            LaunchedEffect(userId) {
                if (userId.isNotEmpty()) {
                    val (nom, prenom) = withContext(Dispatchers.IO) {
                        DatabaseHelper.getEtudiantById(context, userId)
                    }
                    etudiantName = "$prenom $nom"
                }
            }

            MainScaffold(
                userId = userId,
                etudiantName = etudiantName,
                onNavigateToHome = { navController.navigate("home/$userId") },
                onNavigateToSchedule = { navController.navigate("schedule/$userId") }
            ) {
                ScheduleScreen(userId = userId)
            }
        }
    }
}
