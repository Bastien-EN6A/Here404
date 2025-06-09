package com.ironmind.here.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ironmind.here.data.DatabaseHelper
import com.ironmind.here.model.Seance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ScheduleScreen(userId: String, role: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var seances by remember { mutableStateOf<List<Seance>>(emptyList()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(userId, role) {
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                if (role == "prof") {
                    DatabaseHelper.getSeancesPourProf(context, userId)
                } else {
                    DatabaseHelper.getSeancesPourEtudiant(context, userId)
                }
            }
            seances = result
        }
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    val seancesDuJour = seances.filter {
        val date = LocalDateTime.parse(it.debut, formatter).toLocalDate()
        date == selectedDate
    }

    val hourHeight = 64.dp
    val startHour = 8
    val endHour = 18

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { selectedDate = selectedDate.minusDays(1) }) {
                Text("← Précédent")
            }
            Text(text = selectedDate.toString(), style = MaterialTheme.typography.h6)
            Button(onClick = { selectedDate = selectedDate.plusDays(1) }) {
                Text("Suivant →")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(((endHour - startHour) * hourHeight.value).dp)
        ) {
            // Affichage des heures à gauche
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                for (hour in startHour..endHour) {
                    Box(modifier = Modifier.height(hourHeight)) {
                        Text(
                            text = "$hour:00",
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.TopStart).padding(top = 4.dp)
                        )
                    }
                }
            }

            // Ligne verticale centrale
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = 48.dp)
            ) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 4f
                )
            }

            // Affichage des séances
            seancesDuJour.forEach { seance ->
                val debut = LocalDateTime.parse(seance.debut, formatter)
                val fin = LocalDateTime.parse(seance.fin, formatter)
                val durationHours = java.time.Duration.between(debut, fin).toMinutes().toFloat() / 60f
                val offsetHours = debut.hour + debut.minute / 60f - startHour
                val yOffset = (offsetHours * hourHeight.value).dp
                val height = (durationHours * hourHeight.value).dp

                // Résolution du nom du professeur
                val profName = remember(seance.prof_id) {
                    mutableStateOf("")
                }

                LaunchedEffect(seance.prof_id) {
                    withContext(Dispatchers.IO) {
                        profName.value = DatabaseHelper.getProfNameById(context, seance.prof_id)
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(y = yOffset)
                        .padding(start = 64.dp, end = 16.dp, bottom = 4.dp)
                        .fillMaxWidth()
                        .height(height)
                        .background(Color(0xFFB3E5FC), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Text(text = seance.nom, style = MaterialTheme.typography.h6)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = seance.location)
                            Text(text = seance.groupe)
                        }
                        Text(text = "Prof : ${profName.value}", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
