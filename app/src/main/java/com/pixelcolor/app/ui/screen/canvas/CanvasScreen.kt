package com.pixelcolor.app.ui.screen.canvas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CanvasScreen(
    puzzleId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Canvas: $puzzleId", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onComplete) {
                Text("Complete (test)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}
