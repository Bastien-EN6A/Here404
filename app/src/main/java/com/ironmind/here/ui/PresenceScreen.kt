package com.ironmind.here.ui

import android.widget.CheckBox
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.ironmind.here.data.DatabaseHelper
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@Composable
fun PresenceScreen(navController: NavController, seanceId: Int, groupe: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var etudiants by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val checkedStates = remember { mutableStateMapOf<String, Boolean>() }
    var confirmationMessage by remember { mutableStateOf("") }

    // Charger les étudiants du groupe
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                DatabaseHelper.getEtudiantsParGroupe(context, groupe)
            }
            etudiants = result
            result.forEach {
                checkedStates[it.first] = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Bouton retour
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("← Retour")
        }

        Text("Liste de présence - Groupe $groupe", style = MaterialTheme.typography.h6)
        var checked by remember { mutableStateOf(true) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = true,
                onCheckedChange = { checked = it }
            )
            Text("coché = présent")
        }


        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                val allChecked = etudiants.all { checkedStates[it.first] == true }

                // Si tous sont cochés, on décoche tout, sinon on coche tout
                etudiants.forEach { checkedStates[it.first] = !allChecked }
            }) {
                Text("Tout cocher/décocher")
            }

            Button(onClick = {
                confirmationMessage = "Liste des absences envoyée *validée*"
            }) {
                Text("Envoyer absences")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(etudiants) { (id, nomComplet) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checkedStates[id] ?: false,
                        onCheckedChange = { checkedStates[id] = it }
                    )
                    Text(text = nomComplet)
                }
            }
        }

        if (confirmationMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = confirmationMessage, color = Color.Green)
        }
    }
}
