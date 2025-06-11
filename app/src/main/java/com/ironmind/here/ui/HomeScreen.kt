package com.ironmind.here.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ironmind.here.data.DatabaseHelper
import com.ironmind.here.model.Seance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.ironmind.here.model.SeanceStats

@Composable
fun HomeScreen(userId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var nextSeanceProf by remember { mutableStateOf<Seance?>(null) }
    var nextSeanceEtudiant by remember { mutableStateOf<Seance?>(null) }
    var absenceCount by remember { mutableStateOf(0) }
    var totalSeances by remember { mutableStateOf(0) }
    var pastSeancesGrouped by remember { mutableStateOf<Map<String, List<Seance>>>(emptyMap()) }
    var seanceStatsList by remember { mutableStateOf<List<SeanceStats>>(emptyList()) }


    // Hypothèse : les profs ont un ID numérique positif
    val isProf = remember { userId.toIntOrNull() != null && userId.toInt() > 0 }

    LaunchedEffect(userId) {
        coroutineScope.launch {
            if (isProf) {
                val (n, p) = withContext(Dispatchers.IO) {
                    DatabaseHelper.getProfById(context, userId)
                }
                nom = n
                prenom = p

                nextSeanceProf = withContext(Dispatchers.IO) {
                    DatabaseHelper.getNextSeanceForProf(context, userId)
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

                absenceCount = abs
                totalSeances = total
                pastSeancesGrouped = total_Prof
                seanceStatsList = allStats
            } else {
                val (n, p) = withContext(Dispatchers.IO) {
                    DatabaseHelper.getEtudiantById(context, userId)
                }
                nom = n
                prenom = p
                
                nextSeanceEtudiant = withContext(Dispatchers.IO) {
                    DatabaseHelper.getNextSeanceForEtudiant(context, userId)
                }

                absenceCount = withContext(Dispatchers.IO) {
                    DatabaseHelper.getAbsenceByEtudiantId(context, userId)
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top)
    ) {
        Text(
            text = "Bonjour, $prenom $nom",
            style = MaterialTheme.typography.h5.copy(fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        )

        // Affichage pour les professeurs
        if (isProf) {
            nextSeanceProf?.let { seance ->
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm")
                val dateFormatted = try {
                    LocalDateTime.parse(seance.debut).format(formatter)
                } catch (e: Exception) {
                    "Date invalide"
                }

                val backgroundColor = if (isSystemInDarkTheme()) Color(0xFF1E3B2F) else Color(0xFFE8F5E9)
                val textColor = if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    elevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(backgroundColor)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Prochain cours",
                            style = MaterialTheme.typography.subtitle1.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = seance.nom, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        Text(text = dateFormatted, color = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray)
                        Text(text = "Lieu : ${seance.location}", color = if (isSystemInDarkTheme()) Color.LightGray else Color.Gray)
                    }
                }
            } ?: Text("Aucune séance à venir", color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

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
        // Affichage pour les étudiants
        else {
            nextSeanceEtudiant?.let { seance ->
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm")
                val dateFormatted = try {
                    LocalDateTime.parse(seance.debut).format(formatter)
                } catch (e: Exception) {
                    "Date invalide"
                }

                val backgroundColor = if (isSystemInDarkTheme()) Color(0xFF1E3B2F) else Color(0xFFE8F5E9)
                val textColor = if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    elevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(backgroundColor)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Prochain cours",
                            style = MaterialTheme.typography.subtitle1.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = seance.nom, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        Text(text = dateFormatted, color = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray)
                        Text(text = "Lieu : ${seance.location}", color = if (isSystemInDarkTheme()) Color.LightGray else Color.Gray)
                        Text(text = "Groupe : ${seance.groupe}", color = if (isSystemInDarkTheme()) Color.LightGray else Color.Gray)
                    }
                }
            } ?: Text("Aucune séance à venir", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Affichage des statistiques d'absences
            if (totalSeances > 0) {
                Text(
                    text = "Statistiques de présence",
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                PieChart(absences = absenceCount, presences = presenceCount)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val textColor = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Absences : $absenceCount", color = textColor)
                    Text("Présences : $presenceCount", color = textColor)
                    Text("Séances totales : $totalSeances", color = textColor)
                }
            } else {
                Text("Aucune séance passée.", color = Color.Gray)
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
