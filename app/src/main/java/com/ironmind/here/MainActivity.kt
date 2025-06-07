package com.ironmind.here

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ironmind.here.ui.HomeScreen
import com.ironmind.here.ui.LoginScreen
import com.ironmind.here.ui.SplashScreen

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

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(
                onReady = { navController.navigate("login") },
                onError = { /* afficher une erreur si besoin */ }
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
            route = "home/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            HomeScreen(userId = userId)
        }
    }
}
