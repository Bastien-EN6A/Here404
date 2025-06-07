package com.ironmind.here.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ironmind.here.data.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(userId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }

    // Chargement des infos depuis la base
    LaunchedEffect(userId) {
        coroutineScope.launch {
            val result: Pair<String, String> = withContext(Dispatchers.IO) {
                DatabaseHelper.getEtudiantById(context, userId)
            }
            nom = result.first
            prenom = result.second
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp)
                .background(Color.Gray, shape = CircleShape)
        )

        Text(text = "Bienvenue, $prenom $nom" , style = MaterialTheme.typography.h5)
    }
}

