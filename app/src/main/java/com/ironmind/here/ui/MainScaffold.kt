package com.ironmind.here.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun MainScaffold(
    userId: String,
    etudiantName: String,
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToProfile: () -> Unit,
    isDarkTheme: MutableState<Boolean>,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,

        // Top App Bar
        topBar = {
            TopAppBar(
                title = { Text("Here!") },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch { scaffoldState.drawerState.open() }
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    // Affiche le nom de l'utilisateur
                    Text(
                        text = etudiantName,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    // Cercle gris pour avatar, cliquable pour aller au profil
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Gray, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable { onNavigateToProfile() }
                    )
                }
            )
        },

        // Drawer avec navigation
        drawerContent = {
            DrawerContent(
                onNavigateToHome = {
                    scope.launch {
                        scaffoldState.drawerState.close()
                        onNavigateToHome()
                    }
                },
                onNavigateToSchedule = {
                    scope.launch {
                        scaffoldState.drawerState.close()
                        onNavigateToSchedule()
                    }
                },
                onToggleDarkTheme = {
                    isDarkTheme.value = !isDarkTheme.value
                    scope.launch {
                        scaffoldState.drawerState.close()
                    }
                },
                onLogout = {
                    scope.launch {
                        scaffoldState.drawerState.close()
                        onLogout()
                    }
                },
                isDarkTheme = isDarkTheme.value
            )
        },

        // Bottom navigation bar
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigateToHome = onNavigateToHome,
                onNavigateToSchedule = onNavigateToSchedule,
                onNavigateToProfile = onNavigateToProfile,
                isDarkTheme = isDarkTheme.value
            )
        }
    ) { innerPadding ->
        // Contenu de la page
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}
