package com.pixelcolor.app.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPuzzleClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onDailyPuzzleClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PixelColor") },
                actions = {
                    TextButton(onClick = onSettingsClick) {
                        Text("Settings")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Home Screen - Loading puzzles...")
        }
    }
}
