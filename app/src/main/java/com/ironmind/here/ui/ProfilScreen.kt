package com.ironmind.here.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun ProfileScreen(userId: String, name: String) {
    Text("Profil de $name (ID: $userId)")
}
