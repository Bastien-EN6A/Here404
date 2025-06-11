package com.ironmind.here.ui

import androidx.compose.foundation.isSystemInDarkTheme
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigation
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigationItem
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToProfile: () -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color.White
    val contentColor = if (isDarkTheme) Color.White else Color.Black
    BottomNavigation(
        backgroundColor = backgroundColor,
        contentColor = contentColor
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Accueil") },
            selected = currentRoute == "home",
            onClick = onNavigateToHome,
            selectedContentColor = MaterialTheme.colors.primary,
            unselectedContentColor = contentColor.copy(alpha = 0.6f)
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Emploi du temps") },
            selected = currentRoute == "schedule",
            onClick = onNavigateToSchedule,
            selectedContentColor = MaterialTheme.colors.primary,
            unselectedContentColor = contentColor.copy(alpha = 0.6f)
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            selected = currentRoute == "profile",
            onClick = onNavigateToProfile,
            selectedContentColor = MaterialTheme.colors.primary,
            unselectedContentColor = contentColor.copy(alpha = 0.6f)
        )
    }
}