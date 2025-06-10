package com.ironmind.here.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@Composable
fun MainScaffold(
    userId: String,
    etudiantName: String, // ← nom complet de l’étudiant
    onNavigateToHome: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    content: @Composable () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text("Here!")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch { scaffoldState.drawerState.open() }
                        }
                    ) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    // Nom Prénom
                    Text(
                        text = etudiantName,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    // Cercle gris (future photo)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Gray, shape = CircleShape)
                    )
                }
            )
        },
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
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

