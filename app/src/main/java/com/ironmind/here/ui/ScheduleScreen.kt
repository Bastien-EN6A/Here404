package com.ironmind.here.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.ironmind.here.data.DatabaseHelper
import com.ironmind.here.model.Seance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment


@Composable
fun ScheduleScreen(userId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var seances by remember { mutableStateOf<List<Seance>>(emptyList()) }

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val data = withContext(Dispatchers.IO) {
                DatabaseHelper.getSeancesPourEtudiant(context, userId)
            }
            seances = data
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emploi du temps") }
            )
        }
    ) { padding ->
        if (seances.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucune séance à venir.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(seances) { seance ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Cours : ${seance.nom}", style = MaterialTheme.typography.h6)
                            Text("Début : ${seance.debut}")
                            Text("Fin : ${seance.fin}")
                            Text("Lieu : ${seance.location}")
                            Text("Groupe : ${seance.groupe}")
                        }
                    }
                }
            }
        }
    }
}
