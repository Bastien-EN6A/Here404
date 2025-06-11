package com.ironmind.here.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ironmind.here.data.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

@Composable
fun PresenceScreen(
    navController: NavController,
    seanceId: Int,
    groupe: String,
    selectedClassName: String,
    selectedDate: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Liste des étudiants : Pair(id, nom)
    var etudiants by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    // Etat présence : id -> Boolean? (true = présent, false = absent, null = non défini)
    val presenceStates = remember { mutableStateMapOf<String, Boolean?>() }

    var confirmationMessage by remember { mutableStateOf("") }

    val today = remember(selectedDate) { LocalDate.parse(selectedDate) }
    var selectedClass by remember { mutableStateOf(selectedClassName) }

    LaunchedEffect(groupe) {
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                DatabaseHelper.getEtudiantsParGroupe(context, groupe)
            }
            etudiants = result
            result.forEach {
                presenceStates[it.first] = null // initialisé à null (non marqué)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { navController.popBackStack() }) {
                Text("← Retour")
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = selectedClass, style = MaterialTheme.typography.h6)
                Text(text = "Date : $today", style = MaterialTheme.typography.body2)
            }

            Spacer(modifier = Modifier.width(48.dp)) // pour équilibrer l'espace à droite
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Bouton Tout Marquer Présent / Absent / Réinitialiser
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                etudiants.forEach { presenceStates[it.first] = true }
            }) {
                Text("Tout Présent")
            }
            Button(onClick = {
                etudiants.forEach { presenceStates[it.first] = false }
            }) {
                Text("Tout Absent")
            }
            Button(onClick = {
                etudiants.forEach { presenceStates[it.first] = null }
            }) {
                Text("Réinitialiser")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(etudiants) { (id, nomComplet) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = nomComplet,
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    // Bouton Présent
                    Button(
                        onClick = { presenceStates[id] = true },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (presenceStates[id] == true) Color(0xFF4CAF50) else Color.LightGray,
                            contentColor = if (presenceStates[id] == true) Color.White else Color.Black
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("✔️ Présent")
                    }
                    // Bouton Absent
                    Button(
                        onClick = { presenceStates[id] = false },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (presenceStates[id] == false) Color(0xFFF44336) else Color.LightGray,
                            contentColor = if (presenceStates[id] == false) Color.White else Color.Black
                        )
                    ) {
                        Text("❌ Absent")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Enregistrer les absents
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val absents = presenceStates.filter { it.value == false }.keys
                        absents.forEach { etudiantId ->
                            DatabaseHelper.addEtudiantsAbs_seances(
                                context,
                                etudiantId,
                                seanceId
                            )
                        }
                    }
                    confirmationMessage = "Liste des absences envoyée *validée*"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Valider")
        }

        if (confirmationMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = confirmationMessage, color = Color.Green)
        }
    }
}
