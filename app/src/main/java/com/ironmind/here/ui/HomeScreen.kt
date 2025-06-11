package com.ironmind.here.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var nextSeanceProf by remember { mutableStateOf<Seance?>(null) }

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val (n, p) = withContext(Dispatchers.IO) {
                DatabaseHelper.getProfById(context, userId)
            }
            nom = n
            prenom = p

            nextSeanceProf = withContext(Dispatchers.IO) {
                DatabaseHelper.getNextSeanceForProf(context, userId)
            }
        }
    }

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

        nextSeanceProf?.let { seance ->
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm")
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
                        .background(Color(0xFFE8F5E9))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Prochain cours",
                        style = MaterialTheme.typography.subtitle1.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = seance.nom, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    Text(text = dateFormatted, color = Color.DarkGray)
                    Text(text = "Lieu : ${seance.location}", color = Color.Gray)
                }
            }
        } ?: Text("Aucune séance à venir", color = Color.Gray)
    }
}
