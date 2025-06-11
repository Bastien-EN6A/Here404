package com.ironmind.here.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun StudentPresenceCard(
    name: String,
    isPresent: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = MaterialTheme.shapes.medium,
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onToggle(true) }) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Présent",
                        tint = if (isPresent) MaterialTheme.colors.primary else Color.Gray
                    )
                }
                IconButton(onClick = { onToggle(false) }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Absent",
                        tint = if (!isPresent) Color(0xFFC62828) else Color.Gray
                    )
                }
            }
        }
    }
}
