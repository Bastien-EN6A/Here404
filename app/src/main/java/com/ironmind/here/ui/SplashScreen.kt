package com.ironmind.here.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ironmind.here.data.PreloadedDatabaseInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(
    onReady: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val success = withContext(Dispatchers.IO) {
            try {
                PreloadedDatabaseInstaller.copyDatabaseIfNeeded(context)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        if (success) {
            onReady()
        } else {
            error = "Erreur lors de la copie de la base locale"
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (error == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Chargement de la base de données...")
            }
        } else {
            Text(text = error!!, color = MaterialTheme.colors.error)
        }
    }
}
