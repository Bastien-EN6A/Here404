package com.ironmind.here.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DrawerContent(
    onNavigateToHome: () -> Unit,
    onNavigateToSchedule: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Accueil",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToHome() }
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Emploi du temps",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToSchedule() }
                .padding(8.dp)
        )
    }
}
