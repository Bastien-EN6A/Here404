package com.ironmind.here.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DrawerContent(
    onNavigateToHome: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onToggleDarkTheme: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean
) {
    val primaryColor = MaterialTheme.colors.primary
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color.White
    val surfaceColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
    val dividerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFE0E0E0)
    
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(backgroundColor)
    ) {
        // En-tête stylisé avec dégradé
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor,
                            primaryColor.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Text(
                    text = "Here!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Gestion de présence",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation vers l'accueil - Bouton stylisé
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onNavigateToHome() },
            color = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
            elevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Accueil",
                    modifier = Modifier.size(24.dp),
                    tint = primaryColor
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Accueil",
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Spacer à la place du bouton Emploi du temps
        Spacer(modifier = Modifier.height(8.dp))
        
        Divider(
            color = dividerColor,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Bouton pour changer de thème - Bouton stylisé
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onToggleDarkTheme() },
            color = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
            elevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Changer de thème",
                    modifier = Modifier.size(24.dp),
                    tint = if (isDarkTheme) Color(0xFFFFB74D) else Color(0xFF5C6BC0)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (isDarkTheme) "Mode clair" else "Mode sombre",
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Pousse le bouton déconnexion vers le bas

        // Bouton de déconnexion - Bouton stylisé
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onLogout() },
            color = if (isDarkTheme) Color(0xFF3A1010) else Color(0xFFFFEBEE),
            elevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Se déconnecter",
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Se déconnecter",
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}