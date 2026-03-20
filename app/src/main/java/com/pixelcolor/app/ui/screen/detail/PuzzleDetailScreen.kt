package com.pixelcolor.app.ui.screen.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleDetailScreen(
    puzzleId: String,
    onStartClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puzzle Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Puzzle: $puzzleId", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onStartClick) {
                Text("Start Coloring")
            }
        }
    }
}
