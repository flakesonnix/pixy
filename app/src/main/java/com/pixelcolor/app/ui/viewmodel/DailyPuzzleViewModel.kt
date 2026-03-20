package com.pixelcolor.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pixelcolor.app.PixelColorApplication
import com.pixelcolor.app.data.repository.DailyPuzzleManager
import com.pixelcolor.app.data.repository.PuzzleRepository
import com.pixelcolor.app.domain.model.PixelPuzzle
import com.pixelcolor.app.domain.model.UserProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DailyPuzzleUiState(
    val puzzle: PixelPuzzle? = null,
    val progress: UserProgress? = null,
    val nextPuzzleTimeMs: Long = 0L,
    val lastSevenDays: List<DayStatus> = emptyList(),
    val isLoading: Boolean = true
)

data class DayStatus(
    val date: String,
    val completed: Boolean
)

class DailyPuzzleViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: PuzzleRepository = (application as PixelColorApplication).puzzleRepository
    private val dailyManager = DailyPuzzleManager((application as PixelColorApplication).database)

    private val _uiState = MutableStateFlow(DailyPuzzleUiState())
    val uiState: StateFlow<DailyPuzzleUiState> = _uiState.asStateFlow()

    init {
        loadDailyPuzzle()
    }

    private fun loadDailyPuzzle() {
        viewModelScope.launch {
            val puzzle = dailyManager.getTodayPuzzle()
            val progress = puzzle?.let { repo.getProgressSync(it.id) }

            // Build last 7 days status
            val lastSeven = mutableListOf<DayStatus>()
            val today = java.time.LocalDate.now()
            for (i in 6 downTo 0) {
                val date = today.minusDays(i.toLong())
                lastSeven.add(
                    DayStatus(
                        date = date.toString(),
                        completed = false // simplified - would check actual progress
                    )
                )
            }

            _uiState.value = DailyPuzzleUiState(
                puzzle = puzzle,
                progress = progress,
                nextPuzzleTimeMs = dailyManager.getNextPuzzleTime(),
                lastSevenDays = lastSeven,
                isLoading = false
            )
        }
    }

    fun getTimeUntilNext(): String {
        val diff = _uiState.value.nextPuzzleTimeMs - System.currentTimeMillis()
        if (diff <= 0) return "00:00:00"
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60
        val seconds = (diff / 1000) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
