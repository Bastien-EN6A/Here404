package com.ironmind.here.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DrawerContent(
    onNavigateToHome: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onToggleDarkTheme: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color.White
    val dividerColor = if (isDarkTheme) Color.DarkGray else Color.LightGray
    
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Logo ou titre de l'app
        Text(
            text = "Here!",
            modifier = Modifier.padding(8.dp),
            color = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = dividerColor)
        Spacer(modifier = Modifier.height(16.dp))

        // Navigation vers l'accueil
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToHome() }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Accueil",
                modifier = Modifier.padding(end = 16.dp),
                tint = textColor
            )
            Text(text = "Accueil", color = textColor)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Spacer à la place du bouton Emploi du temps
        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = dividerColor)
        Spacer(modifier = Modifier.height(16.dp))

        // Bouton pour changer de thème
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleDarkTheme() }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Changer de thème",
                modifier = Modifier.padding(end = 16.dp),
                tint = textColor
            )
            Text(text = if (isDarkTheme) "Mode clair" else "Mode sombre", color = textColor)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bouton de déconnexion
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogout() }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Se déconnecter",
                tint = Color(0xFFC62828),
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = "Se déconnecter",
                color = Color(0xFFC62828)
            )
        }
    }
}