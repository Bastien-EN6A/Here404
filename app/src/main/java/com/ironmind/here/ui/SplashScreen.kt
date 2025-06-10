package com.ironmind.here.ui

import androidx.compose.foundation.layout.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ironmind.here.data.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import com.airbnb.lottie.compose.*
import com.ironmind.here.R

@Composable
fun SplashScreen(
    onReady: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    // Animation Lottie
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_intro))
    val progress by animateLottieCompositionAsState(composition)

    // Copie de la base + délai
    LaunchedEffect(Unit) {
        val success = withContext(Dispatchers.IO) {
            try {
                DatabaseHelper.copyDatabaseIfNeeded(context)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        if (success) {
            delay(1800) // ⏳ délai ajouté ici
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
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Chargement de la base de données...")
            }
        } else {
            Text(text = error!!, color = MaterialTheme.colors.error)
        }
    }
}
