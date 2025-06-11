package com.ironmind.here.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ironmind.here.data.DatabaseHelper
import com.ironmind.here.model.Seance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(userId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var absenceCount by remember { mutableStateOf(0) }
    var totalSeances by remember { mutableStateOf(0) }
    var pastSeancesGrouped by remember { mutableStateOf<Map<String, List<Seance>>>(emptyMap()) }
    var nextSeanceProf by remember { mutableStateOf<Seance?>(null) }

    // Hypothèse : les profs ont un nom/prénom mais pas d'entrée dans la table d'absences
    val isProf = remember { userId.toIntOrNull() != null && userId.toInt() > 0 }

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val (n, p) = withContext(Dispatchers.IO) {
                DatabaseHelper.getEtudiantById(context, userId)
            }
            nom = n
            prenom = p

            if (isProf) {
                nextSeanceProf = withContext(Dispatchers.IO) {
                    DatabaseHelper.getNextSeanceForProf(context, userId)
                }
                pastSeancesGrouped = withContext(Dispatchers.IO) {
                    DatabaseHelper.getPastSeancesGroupedByDebut(context, userId)
                }
            } else {
                absenceCount = withContext(Dispatchers.IO) {
                    DatabaseHelper.getNombreSeancesPasseesPourEtudiant(context, userId)
                }
                totalSeances = withContext(Dispatchers.IO) {
                    DatabaseHelper.getNombreSeancesPasseesPourEtudiant(context, userId)
                }
            }
        }
    }

    val presenceCount = (totalSeances - absenceCount).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Bienvenue, $prenom $nom", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(24.dp))

        // Prof: prochaine séance
        if (isProf && nextSeanceProf != null) {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            val dateFormatted = try {
                LocalDateTime.parse(nextSeanceProf!!.debut).format(formatter)
            } catch (e: Exception) {
                "Date invalide"
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Prochaine séance", fontWeight = FontWeight.Bold)
                Text("${nextSeanceProf!!.nom} – $dateFormatted", color = Color(0xFF2E7D32))
                Text("Lieu : ${nextSeanceProf!!.location}")
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (!isProf) {
            if (totalSeances > 0) {
                PieChart(absenceCount, presenceCount)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Absences : $absenceCount")
                Text("Présences : $presenceCount")
                Text("Séances totales : $totalSeances")
            } else {
                Text("Aucune séance passée.")
            }
        } else {
            Text("Séances passées (groupées par créneau) : ${pastSeancesGrouped.size}")
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                pastSeancesGrouped.forEach { (debut, seances) ->
                    item {
                        Text("Créneau : $debut", fontWeight = FontWeight.Bold)
                    }
                    items(seances, key = { it.id }) { seance ->
                        Text(" - ${seance.nom} (${seance.groupe}) à ${seance.location}")
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(absences: Int, presences: Int) {
    val total = (absences + presences).takeIf { it > 0 } ?: return
    val absenceAngle = (absences.toFloat() / total) * 360f
    val presenceAngle = 360f - absenceAngle

    Canvas(modifier = Modifier.size(200.dp)) {
        val size = Size(size.width, size.height)
        drawArc(
            color = Color(0xFFC62828), // Rouge pour absences
            startAngle = 0f,
            sweepAngle = absenceAngle,
            useCenter = true,
            size = size
        )
        drawArc(
            color = Color(0xFF2E7D32), // Vert pour présences
            startAngle = absenceAngle,
            sweepAngle = presenceAngle,
            useCenter = true,
            size = size
        )
    }
}
