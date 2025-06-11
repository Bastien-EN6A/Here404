package com.ironmind.here.ui

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.ironmind.here.R
import com.ironmind.here.data.DatabaseHelper

@Composable
fun ProfileScreen(
    userId: String,
    name: String,
    onLogout: () -> Unit = {},
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    // Données à afficher
    val (email, groupeTd, groupeTp) = remember(userId) {
        getEtudiantInfos(context, userId)
    }

    // Couleurs adaptées au thème
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val primaryColor = Color(0xFF2E7D32)
    val secondaryTextColor = if (isDarkTheme) Color.LightGray else Color.Gray

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor.copy(alpha = 0.85f))
                .zIndex(1f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .zIndex(2f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
                    .padding(20.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            )

            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium.copy(color = secondaryTextColor)
            )

            Spacer(Modifier.height(24.dp))

            ProfileItem("ID étudiant", userId, isDarkTheme)
            ProfileItem("Groupe TD", groupeTd, isDarkTheme)
            ProfileItem("Groupe TP", groupeTp, isDarkTheme)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
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

@Composable
fun ProfileItem(label: String, value: String, isDarkTheme: Boolean = isSystemInDarkTheme()) {
    val labelColor = if (isDarkTheme) Color.LightGray else Color.DarkGray
    val valueColor = if (isDarkTheme) Color.White else Color.Black
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = labelColor)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(color = valueColor)
        )
    }
}

fun getEtudiantInfos(context: Context, userId: String): Triple<String, String, String> {
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
        e.printStackTrace()
        Triple("Erreur", "Erreur", "Erreur")
    } finally {
        db.close()
    }
}
