package com.ironmind.here.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun HomeScreen(userId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var prochaineSeance by remember { mutableStateOf<Seance?>(null) }
    var absenceCount by remember { mutableStateOf(0) }
    var totalSeances by remember { mutableStateOf(0) }

    // Identifier le rôle par convention
    val isProf = remember { userId.toIntOrNull() != null && userId.toInt() > 0 }

    LaunchedEffect(userId) {
        coroutineScope.launch {
            if (isProf) {
                val (n, p) = withContext(Dispatchers.IO) {
                    DatabaseHelper.getProfById(context, userId)
                }
                nom = n
                prenom = p
                prochaineSeance = withContext(Dispatchers.IO) {
                    DatabaseHelper.getNextSeanceForProf(context, userId)
                }
            } else {
                val (n, p) = withContext(Dispatchers.IO) {
                    DatabaseHelper.getEtudiantById(context, userId)
                }
                nom = n
                prenom = p
                prochaineSeance = withContext(Dispatchers.IO) {
                    DatabaseHelper.getNextSeanceForEtudiant(context, userId)
                }
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
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm")
    val darkTheme = isSystemInDarkTheme()
    val bgCard = if (darkTheme) Color(0xFF1E3B2F) else Color(0xFFE8F5E9)
    val textColor = if (darkTheme) Color.LightGray else Color.DarkGray
    val accentColor = if (darkTheme) Color(0xFF81C784) else Color(0xFF2E7D32)

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

        // Carte unique pour prochaine séance
        prochaineSeance?.let { seance ->
            val dateFormatted = try {
                LocalDateTime.parse(seance.debut).format(formatter)
            } catch (e: Exception) {
                "Date invalide"
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                elevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(bgCard)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Prochain cours",
                        style = MaterialTheme.typography.subtitle1.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = seance.nom, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    Text(text = dateFormatted, color = textColor)
                    Text(text = "Lieu : ${seance.location}", color = textColor)
                    if (!isProf) {
                        Text(text = "Groupe : ${seance.groupe}", color = textColor)
                    }
                }
            }
        } ?: Text("Aucune séance à venir", color = Color.Gray)

        // Stats de présence (étudiant uniquement)
        if (!isProf && totalSeances > 0) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Statistiques de présence",
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.padding(top = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                PieChart(absences = absenceCount, presences = presenceCount)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Absences : $absenceCount", color = textColor)
                Text("Présences : $presenceCount", color = textColor)
                Text("Séances totales : $totalSeances", color = textColor)
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
