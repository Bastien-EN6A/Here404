package com.ironmind.here.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ironmind.here.data.DatabaseHelper
import com.ironmind.here.model.Seance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.ironmind.here.model.SeanceStats
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
    var seanceStatsList by remember { mutableStateOf<List<SeanceStats>>(emptyList()) }


    LaunchedEffect(userId) {
        coroutineScope.launch {
            val (n, p) = withContext(Dispatchers.IO) {
                DatabaseHelper.getEtudiantById(context, userId)
            }
            val abs = withContext(Dispatchers.IO) {
                DatabaseHelper.getAbsenceByEtudiantId(context, userId)
            }
            val total = withContext(Dispatchers.IO) {
                DatabaseHelper.getNombreSeancesPasseesPourEtudiant(context, userId)
            }
            val total_Prof = withContext(Dispatchers.IO) {
                DatabaseHelper.getPastSeancesGroupedByDebut(context, userId)
            }
            val seancesByDebut = withContext(Dispatchers.IO) {
                DatabaseHelper.getPastSeancesGroupedByDebut(context, userId)
            }

            val allStats = mutableListOf<SeanceStats>()

            for ((_, seances) in seancesByDebut) {
                for (seance in seances) {
                    val etudiants = withContext(Dispatchers.IO) {
                        DatabaseHelper.getEtudiantsParGroupe(context, seance.groupe)
                    }

                    val absents = withContext(Dispatchers.IO) {
                        DatabaseHelper.getAbsencesForSeance(context, seance.id)
                    }

                    val totalEtudiants = etudiants.size
                    val absentCount = absents.size
                    val presentCount = (totalEtudiants - absentCount).coerceAtLeast(0)

                    allStats.add(SeanceStats(seance, absentCount, presentCount))
                }
            }

            nom = n
            prenom = p
            absenceCount = abs
            totalSeances = total
            pastSeancesGrouped = total_Prof
            seanceStatsList = allStats
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

        if (totalSeances > 0) {
            PieChart(absenceCount, presenceCount)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Absences : $absenceCount")
            Text("Présences : $presenceCount")
            Text("Séances totales : $totalSeances")
        } else {
            Text("Séances passées (groupées par créneau) : ${pastSeancesGrouped.size}")

            LazyColumn(
                modifier = Modifier.padding(16.dp)
            ) {
                items(seanceStatsList) { stat ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "${stat.seance.nom} (${stat.seance.groupe}) - ${stat.seance.debut}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.body1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PieChart(absences = stat.absents, presences = stat.presents)


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
