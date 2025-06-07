package com.ironmind.here.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun MainScaffold(
    userId: String,
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
                title = { Text("Here App") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch { scaffoldState.drawerState.open() }
                        }
                    ) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
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
