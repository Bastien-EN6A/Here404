package com.ironmind.here.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ironmind.here.data.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ScheduleScreen(userId: String) {
    val context = LocalContext.current
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        val (n, p) = withContext(Dispatchers.IO) {
            DatabaseHelper.getEtudiantById(context, userId)
        }
        nom = n
        prenom = p
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Emploi du temps de $prenom $nom", style = MaterialTheme.typography.h5)
    }
}
