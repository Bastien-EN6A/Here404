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
import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import java.util.Locale
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SelectableDates
import java.time.Instant
import java.time.ZoneId
import com.ironmind.here.data.ScheduleState


@Composable
fun ScheduleScreen(userId: String, role: String, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var seances by remember { mutableStateOf<List<Seance>>(emptyList()) }
    // Utiliser l'état partagé pour la date sélectionnée
    var selectedDate by remember { ScheduleState.selectedDate }
    var showDatePicker by remember { mutableStateOf(false) }

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
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)
    val seancesDuJour = seances.filter {
        val date = LocalDateTime.parse(it.debut, formatter).toLocalDate()
        date == selectedDate
    }

    val hourHeight = 64.dp
    val startHour = 8
    val endHour = 20

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { selectedDate = selectedDate.minusDays(1) },
                modifier = Modifier.size(width = 110.dp, height = 40.dp)
            ) {
                Text("Précédent")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showDatePicker = true }
            ) {
                Text(
                    text = selectedDate.format(dateFormatter),
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Sélectionner une date",
                    tint = MaterialTheme.colors.primary
                )
            }
            Button(
                onClick = { selectedDate = selectedDate.plusDays(1) },
                modifier = Modifier.size(width = 100.dp, height = 40.dp)
            ) {
                Text("Suivant")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Affichage du sélecteur de date
        if (showDatePicker) {
            DatePickerDialogComponent(
                onDateSelected = { date ->
                    selectedDate = date
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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
                        .border(1.dp, Color(0xFF03A9F4), RoundedCornerShape(12.dp))
                        .clickable(enabled = role == "prof") {
                            val formattedDate = selectedDate.format(DateTimeFormatter.ISO_DATE)
                            navController.navigate("presence/${seance.id}/${seance.groupe}/${seance.nom}/$formattedDate")

                        }
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Text(text = seance.nom, style = MaterialTheme.typography.h6, color = Color(0xFF01579B))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = seance.location, color = Color(0xFF01579B))
                            Text(text = seance.groupe, color = Color(0xFF01579B))
                        }
                        Text(text = "Prof : ${profName.value}", fontSize = 12.sp, color = Color(0xFF01579B))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogComponent(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val localDate = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(localDate)
                } ?: onDismiss()
            }) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
