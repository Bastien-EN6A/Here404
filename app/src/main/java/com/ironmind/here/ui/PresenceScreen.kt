package com.ironmind.here.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import com.ironmind.here.data.DatabaseHelper
import com.ironmind.here.data.ScheduleState

@Composable
fun PresenceScreen(
    navController: NavController,
    seanceId: Int,
    groupe: String,
    selectedClassName: String,
    selectedDate: String,
    isDarkTheme: Boolean = isSystemInDarkTheme()
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
    
    // Mettre à jour la date sélectionnée dans l'état partagé
    LaunchedEffect(selectedDate) {
        ScheduleState.selectedDate.value = today
    }

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

    // Définir les couleurs en fonction du mode sombre
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF6FFF8)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val dividerColor = if (isDarkTheme) Color(0xFF424242) else Color.LightGray
    val buttonBackgroundColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.LightGray
    val presentColor = Color(0xFF4CAF50) // Vert pour présent
    val absentColor = Color(0xFFF44336) // Rouge pour absent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
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
                Text("Retour")
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = selectedClass, style = MaterialTheme.typography.h6, color = textColor)
                Text(text = "Date : $today", style = MaterialTheme.typography.body2, color = textColor)
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
            },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = presentColor
                )
                ) {
                Text("Tout Présent")
            }
            Button(onClick = {
                etudiants.forEach { presenceStates[it.first] = false }
            },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = absentColor)
                ) {
                Text("Tout Absent")
            }
            Button(onClick = {
                etudiants.forEach { presenceStates[it.first] = null }
            },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = buttonBackgroundColor,
                    contentColor = textColor)
                    ) {
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
                        .padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = nomComplet,
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    // Bouton Présent
                    Button(
                        onClick = { presenceStates[id] = true },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (presenceStates[id] == true) presentColor else buttonBackgroundColor,
                            contentColor = if (presenceStates[id] == true) Color.White else textColor
                        ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(1.dp), // évite l'expansion inutile du bouton
                        modifier = Modifier
                            .size(45.dp) // largeur = hauteur → bouton rond
                            .padding(end = 1.dp)
                    ) {
                        Text("✔️")
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Bouton Absent
                    Button(
                        onClick = { presenceStates[id] = false },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (presenceStates[id] == false) absentColor else buttonBackgroundColor,
                            contentColor = if (presenceStates[id] == false) Color.White else textColor
                        ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(1.dp), // évite l'expansion inutile du bouton
                        modifier = Modifier
                            .size(45.dp) // largeur = hauteur → bouton rond
                            .padding(end = 1.dp)
                    ) {
                        Text("❌")
                    }
                }
                Divider(
                    color = dividerColor,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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

                    DatabaseHelper.UpdateRasp(context) //on update le sftp
                    confirmationMessage = "Liste des absences envoyée *validée*"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Valider", color = Color.White)
        }

        if (confirmationMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = confirmationMessage, color = if (isDarkTheme) Color(0xFF81C784) else Color.Green)
        }
    }
}
