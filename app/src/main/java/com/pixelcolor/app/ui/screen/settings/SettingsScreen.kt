package com.pixelcolor.app.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pixelcolor.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Audio & Feedback section
            SectionHeader("Audio & Feedback")

            SettingsSwitch(
                title = "Sound Effects",
                subtitle = "Tap sounds and completion chimes",
                checked = uiState.soundEnabled,
                onCheckedChange = viewModel::setSoundEnabled
            )

            SettingsSwitch(
                title = "Haptic Feedback",
                subtitle = "Vibration on pixel fill",
                checked = uiState.hapticEnabled,
                onCheckedChange = viewModel::setHapticEnabled
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Gameplay section
            SectionHeader("Gameplay")

            SettingsSwitch(
                title = "Auto-zoom on Start",
                subtitle = "Automatically zoom to fit puzzle",
                checked = uiState.autoZoom,
                onCheckedChange = viewModel::setAutoZoom
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Accessibility section
            SectionHeader("Accessibility")

            SettingsSwitch(
                title = "Color-blind Mode",
                subtitle = "Adds shape patterns to palette circles",
                checked = uiState.colorBlindMode,
                onCheckedChange = viewModel::setColorBlindMode
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Theme section
            SectionHeader("Theme")

            ThemeSelector(
                currentTheme = uiState.themeMode,
                onThemeSelected = viewModel::setThemeMode
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // About section
            SectionHeader("About")

            SettingsInfo("Version", "1.0.0")
            SettingsInfo("Build", "1")
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsInfo(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ThemeSelector(
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf(
        "system" to "System",
        "light" to "Light",
        "dark" to "Dark"
    )

    Column(modifier = Modifier.selectableGroup()) {
        themes.forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentTheme == value,
                        onClick = { onThemeSelected(value) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentTheme == value,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
