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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread {
            DatabaseHelper.UpdateLocal(this)  //on met a jour la base de donnée locale
        }.start()
        setContent {
            MainApp()
        }
    }
}

@Composable
fun MainApp() {
    // Utiliser directement isSystemInDarkTheme() dans le corps de la fonction composable
    val isDarkMode = isSystemInDarkTheme()
    val isDarkTheme = remember { mutableStateOf(isDarkMode) }

    CustomAppTheme(darkTheme = isDarkTheme.value) {
        HereApp(isDarkTheme = isDarkTheme)
    }
}

@Composable
fun CustomAppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        darkColors(
            primary = Color(0xFF2E7D32),
            primaryVariant = Color(0xFF1B5E20),
            secondary = Color(0xFF66BB6A),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
    } else {
        lightColors(
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
    }

    MaterialTheme(
        colors = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

@Composable
fun HereApp(isDarkTheme: MutableState<Boolean>) {
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

            val context = LocalContext.current
            val fullName = remember(userId, role) {
                if (role == "prof") {
                    val (nom, prenom) = DatabaseHelper.getProfById(context, userId)
                    "$prenom $nom"
                } else {
                    displayName
                }
            }

            MainScaffold(
                userId = userId,
                etudiantName = fullName,
                currentRoute = currentSubRoute,
                onNavigateToHome = { subNavController.navigate("home") },
                onNavigateToSchedule = { subNavController.navigate("schedule") },
                onNavigateToProfile = { subNavController.navigate("profile") },
                isDarkTheme = isDarkTheme,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
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
                            },
                            isDarkTheme = isDarkTheme.value
                        )
                    }
                }
            }
        }
        composable(
            "presence/{seanceId}/{groupe}/{coursNom}/{date}",
            arguments = listOf(
                navArgument("seanceId") { type = NavType.IntType },
                navArgument("groupe") { type = NavType.StringType },
                navArgument("coursNom") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val seanceId = backStackEntry.arguments?.getInt("seanceId") ?: 0
            val groupe = backStackEntry.arguments?.getString("groupe") ?: ""
            val coursNom = backStackEntry.arguments?.getString("coursNom") ?: ""
            val date = backStackEntry.arguments?.getString("date") ?: ""

            PresenceScreen(
                navController = navController,
                seanceId = seanceId,
                groupe = groupe,
                selectedClassName = coursNom,
                selectedDate = date,
                isDarkTheme = isDarkTheme.value
            )
        }
    }
}
