package com.ironmind.here.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ironmind.here.data.DatabaseHelper
import com.ironmind.here.model.Seance
import com.ironmind.here.model.SeanceStats
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
    var nextSeance by remember { mutableStateOf<Seance?>(null) }
    var absenceCount by remember { mutableStateOf(0) }
    var totalSeances by remember { mutableStateOf(0) }
    var seanceStatsList by remember { mutableStateOf<List<SeanceStats>>(emptyList()) }

    val isProf = remember { userId.toIntOrNull() != null && userId.toInt() > 0 }

    LaunchedEffect(userId) {
        coroutineScope.launch {
            if (isProf) {
                val (n, p) = withContext(Dispatchers.IO) { DatabaseHelper.getProfById(context, userId) }
                nom = n
                prenom = p
                nextSeance = withContext(Dispatchers.IO) { DatabaseHelper.getNextSeanceForProf(context, userId) }
                val seancesByDebut = withContext(Dispatchers.IO) { DatabaseHelper.getPastSeancesGroupedByDebut(context, userId) }

                val stats = mutableListOf<SeanceStats>()
                for ((_, seances) in seancesByDebut) {
                    for (seance in seances) {
                        if (seance.prof_id.toString() != userId) continue
                        val etudiants = withContext(Dispatchers.IO) {
                            DatabaseHelper.getEtudiantsParGroupe(context, seance.groupe)
                        }
                        val absents = withContext(Dispatchers.IO) {
                            DatabaseHelper.getAbsencesForSeance(context, seance.id)
                        }
                        val totalEtudiants = etudiants.size
                        val absentCount = absents.size
                        val presentCount = (totalEtudiants - absentCount).coerceAtLeast(0)
                        stats.add(SeanceStats(seance, absentCount, presentCount))
                    }
                }
                seanceStatsList = stats
            } else {
                val (n, p) = withContext(Dispatchers.IO) { DatabaseHelper.getEtudiantById(context, userId) }
                nom = n
                prenom = p
                nextSeance = withContext(Dispatchers.IO) { DatabaseHelper.getNextSeanceForEtudiant(context, userId) }
                absenceCount = withContext(Dispatchers.IO) { DatabaseHelper.getAbsenceByEtudiantId(context, userId) }
                totalSeances = withContext(Dispatchers.IO) { DatabaseHelper.getNombreSeancesPasseesPourEtudiant(context, userId) }
            }
        }
    }

    val presenceCount = (totalSeances - absenceCount).coerceAtLeast(0)
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm")

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top)
    ) {
        Text(
            text = "Bonjour, $prenom $nom",
            style = MaterialTheme.typography.h5.copy(fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        )

        nextSeance?.let { seance ->
            val backgroundColor = if (isSystemInDarkTheme()) Color(0xFF1E3B2F) else Color(0xFFE8F5E9)
            val textColor = if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)
            val dateFormatted = try {
                LocalDateTime.parse(seance.debut).format(formatter)
            } catch (e: Exception) { "Date invalide" }

            Surface(
                shape = RoundedCornerShape(16.dp),
                elevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.background(backgroundColor).padding(20.dp)
                ) {
                    Text("Prochain cours", fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(seance.nom, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    Text(dateFormatted, color = Color.Gray)
                    Text("Lieu : ${seance.location}", color = Color.Gray)
                    if (!isProf) Text("Groupe : ${seance.groupe}", color = Color.Gray)
                }
            }
        } ?: Text("Aucune séance à venir", color = Color.Gray)

        if (isProf && seanceStatsList.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items = seanceStatsList, key = { it.seance.id }) { stat ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        elevation = 6.dp,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("${stat.seance.nom} - ${stat.seance.groupe}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            PieChartWithLegend(stat.absents, stat.presents)
                        }
                    }
                }
            }
        } else if (!isProf && totalSeances > 0) {
            Text("Statistiques de présence", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            PieChartWithLegend(absences = absenceCount, presences = presenceCount)
            Spacer(modifier = Modifier.height(16.dp))
            val statColor = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray
            Text("Absences : $absenceCount", color = statColor)
            Text("Présences : $presenceCount", color = statColor)
            Text("Séances totales : $totalSeances", color = statColor)
        } else if (!isProf) {
            Text("Aucune séance passée.", color = Color.Gray)
        }
    }
}

@Composable
fun PieChartWithLegend(absences: Int, presences: Int) {
    val total = absences + presences
    if (total == 0) return

    val absenceAngle = (absences.toFloat() / total) * 360f
    val presenceAngle = 360f - absenceAngle
    val absencePercent = (absences.toFloat() / total * 100).toInt()
    val presencePercent = 100 - absencePercent

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .size(180.dp)
                .padding(16.dp)
        ) {
            val radius = size.minDimension / 2
            val innerRadius = radius * 0.6f
            val center = this.center

            drawArc(
                color = Color(0xFFE53935),
                startAngle = 0f,
                sweepAngle = absenceAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            drawArc(
                color = Color(0xFF43A047),
                startAngle = absenceAngle,
                sweepAngle = presenceAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                center = center
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(color = Color(0xFF43A047), label = "Présent ($presencePercent%)")
            LegendItem(color = Color(0xFFE53935), label = "Absent ($absencePercent%)")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 14.sp)
    }
}