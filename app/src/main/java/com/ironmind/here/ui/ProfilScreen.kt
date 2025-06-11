package com.ironmind.here.ui

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.ironmind.here.R
import com.ironmind.here.data.DatabaseHelper

@Composable
fun ProfileScreen(userId: String, role: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val (fullName, email, groupeTd, groupeTp) = remember(userId, role) {
        when (role) {
            "etudiant" -> {
                val (nom, prenom) = DatabaseHelper.getEtudiantById(context, userId)
                val name = "$prenom $nom"
                val (mail, td, tp) = getEtudiantDetails(context, userId)
                Quadruple(name, mail, td, tp)
            }
            "prof" -> {
                val nom = DatabaseHelper.getProfNameById(context, userId.toIntOrNull() ?: -1)
                val mail = getProfEmail(context, userId)
                Quadruple(nom, mail, "", "")
            }
            else -> Quadruple("Inconnu", "Inconnu", "", "")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.85f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
                        color = Color(0xFF2E7D32)
                    )
                )

                Text(
                    text = "ID : $userId",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )

                Spacer(Modifier.height(24.dp))

                ProfileItem("Adresse email", email)

                if (role == "etudiant") {
                    ProfileItem("Groupe TD", groupeTd)
                    ProfileItem("Groupe TP", groupeTp)
                }
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
                title = { Text("Confirmer la déconnexion") },
                text = { Text("Voulez-vous vraiment vous déconnecter ?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        onLogout()
                    }) {
                        Text("Oui")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

fun getEtudiantDetails(context: Context, userId: String): Triple<String, String, String> {
    val dbPath = context.getDatabasePath("will_emploi_temps_final.db").absolutePath
    val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

    return try {
        val cursor = db.rawQuery(
            "SELECT email, groupe_td, groupe_tp FROM etudiants WHERE id = ?",
            arrayOf(userId)
        )
        if (cursor.moveToFirst()) {
            Triple(
                cursor.getString(0) ?: "Inconnu",
                cursor.getString(1) ?: "Inconnu",
                cursor.getString(2) ?: "Inconnu"
            )
        } else {
            Triple("Non trouvé", "Non trouvé", "Non trouvé")
        }
    } catch (e: Exception) {
        Triple("Erreur", "Erreur", "Erreur")
    } finally {
        db.close()
    }
}

fun getProfEmail(context: Context, profId: String): String {
    val dbPath = context.getDatabasePath("will_emploi_temps_final.db").absolutePath
    val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

    return try {
        val cursor = db.rawQuery(
            "SELECT email FROM profs WHERE id = ?",
            arrayOf(profId)
        )
        if (cursor.moveToFirst()) {
            cursor.getString(0) ?: "Inconnu"
        } else {
            "Non trouvé"
        }
    } catch (e: Exception) {
        "Erreur"
    } finally {
        db.close()
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun ProfileItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
        )
    }
}
