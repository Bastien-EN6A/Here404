package com.ironmind.here.ui

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.foundation.isSystemInDarkTheme
import com.ironmind.here.R
import com.ironmind.here.data.DatabaseHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(userId: String, role: String, onLogout: () -> Unit, isDarkTheme: Boolean = isSystemInDarkTheme()) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val (nom, prenom) = remember(userId, role) {
        if (role == "etudiant") DatabaseHelper.getEtudiantById(context, userId)
        else DatabaseHelper.getProfById(context, userId)
    }

    val (email, groupeTd, groupeTp) = remember(userId, role) {
        if (role == "etudiant") getInfosEtudiant(context, userId)
        else Triple(getEmailProf(context, userId), "", "")
    }

    val fullName = "$prenom $nom"

    val nextSeance = remember(userId, role) {
        if (role == "prof") DatabaseHelper.getNextSeanceForProf(context, userId) else null
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().zIndex(0f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkTheme) Color.Black.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f))
                .zIndex(1f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .zIndex(2f),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(32.dp))
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32))
                        .padding(20.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = fullName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color(0xFF2E7D32)
                    )
                )

                Text(
                    text = "ID : $userId",
                    style = MaterialTheme.typography.bodyMedium.copy(color = if (isDarkTheme) Color.LightGray else Color.Gray)
                )

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
                    contentColor = if (isDarkTheme) Color.White else Color.Black
                ),
                border = if (isDarkTheme) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)) else null,
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val formattedDate = nextSeance?.debut?.let {
                            try {
                                val inputFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                val outputFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                LocalDateTime.parse(it, inputFormat).format(outputFormat)
                            } catch (e: Exception) {
                                "Date invalide"
                            }
                        }
                        ProfileItem("Email", email, isDarkTheme)
                        if (role == "etudiant") {
                            ProfileItem("Groupe TD", groupeTd, isDarkTheme)
                            ProfileItem("Groupe TP", groupeTp, isDarkTheme)
                        }
                        if (role == "prof" && nextSeance != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            ProfileItem("Prochain cours", "${nextSeance.nom} • $formattedDate", isDarkTheme)
                        }

                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "\"L'absence forge la mémoire de la présence.\"",
                    fontStyle = FontStyle.Italic,
                    color = if (isDarkTheme) Color(0xFF66BB6A) else Color(0xFF2E7D32),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 24.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Se déconnecter", color = Color.White)
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
                title = { Text("Confirmer la déconnexion", color = if (isDarkTheme) Color.White else Color.Black) },
                text = { Text("Voulez-vous vraiment vous déconnecter ?", color = if (isDarkTheme) Color.White else Color.DarkGray) },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        onLogout()
                    }) {
                        Text("Oui", color = if (isDarkTheme) Color(0xFF81C784) else Color(0xFF2E7D32))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Annuler", color = if (isDarkTheme) Color.LightGray else Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String, isDarkTheme: Boolean = isSystemInDarkTheme()) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isDarkTheme) Color.White else Color.DarkGray,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                color = if (isDarkTheme) Color.White else Color.Black,
                fontWeight = if (isDarkTheme) FontWeight.Medium else FontWeight.Normal
            )
        )
    }
}

// Prof only
fun getEmailProf(context: Context, id: String): String {
    val db = SQLiteDatabase.openDatabase(
        context.getDatabasePath("will_emploi_temps_final.db").absolutePath,
        null,
        SQLiteDatabase.OPEN_READONLY
    )
    return try {
        val cursor = db.rawQuery("SELECT email FROM profs WHERE id = ?", arrayOf(id))
        if (cursor.moveToFirst()) cursor.getString(0) else "Non trouvé"
    } catch (e: Exception) {
        "Erreur"
    } finally {
        db.close()
    }
}

fun getInfosEtudiant(context: Context, userId: String): Triple<String, String, String> {
    val dbPath = context.getDatabasePath("will_emploi_temps_final.db").absolutePath
    val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

    return try {
        val cursor = db.rawQuery(
            "SELECT email, groupe_td, groupe_tp FROM etudiants WHERE id = ?",
            arrayOf(userId)
        )
        if (cursor.moveToFirst()) {
            Triple(cursor.getString(0) ?: "Inconnu", cursor.getString(1) ?: "Inconnu", cursor.getString(2) ?: "Inconnu")
        } else {
            Triple("Non trouvé", "Non trouvé", "Non trouvé")
        }
    } catch (e: Exception) {
        Triple("Erreur", "Erreur", "Erreur")
    } finally {
        db.close()
    }
}
