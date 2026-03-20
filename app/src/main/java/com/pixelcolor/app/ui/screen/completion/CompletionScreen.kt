package com.pixelcolor.app.ui.screen.completion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CompletionScreen(
    puzzleId: String,
    onBackToHome: () -> Unit,
    onNextPuzzle: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Congratulations!", style = MaterialTheme.typography.displayMedium)
            Text("Puzzle $puzzleId completed!")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBackToHome) {
                Text("Back to Home")
            }
        }
    }
}
