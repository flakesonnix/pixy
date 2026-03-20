package com.pixelcolor.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pixelcolor.app.PixelColorApplication
import com.pixelcolor.app.data.repository.PuzzleRepository
import com.pixelcolor.app.domain.model.Category
import com.pixelcolor.app.domain.model.PixelPuzzle
import com.pixelcolor.app.domain.model.UserProgress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PuzzleWithProgress(
    val puzzle: PixelPuzzle,
    val progress: UserProgress?
)

enum class SortOrder { NEW, EASIEST, HARDEST, IN_PROGRESS }

data class HomeUiState(
    val puzzles: List<PuzzleWithProgress> = emptyList(),
    val selectedCategory: Category? = null,
    val sortOrder: SortOrder = SortOrder.NEW,
    val dailyStreak: Int = 0,
    val isLoading: Boolean = true
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: PuzzleRepository = (application as PixelColorApplication).puzzleRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.initializePuzzles()
            observePuzzles()
        }
    }

    private fun observePuzzles() {
        viewModelScope.launch {
            combine(
                repo.getAllPuzzles(),
                repo.getAllProgress(),
                _uiState.map { it.selectedCategory }.distinctUntilChanged(),
                _uiState.map { it.sortOrder }.distinctUntilChanged()
            ) { puzzles, progressList, category, sort ->
                val progressMap = progressList.associateBy { it.puzzleId }
                var filtered = puzzles.map { p -> PuzzleWithProgress(p, progressMap[p.id]) }

                if (category != null) {
                    filtered = filtered.filter { it.puzzle.category == category }
                }

                filtered = when (sort) {
                    SortOrder.NEW -> filtered.sortedByDescending { it.puzzle.id }
                    SortOrder.EASIEST -> filtered.sortedBy { it.puzzle.difficulty.ordinal }
                    SortOrder.HARDEST -> filtered.sortedByDescending { it.puzzle.difficulty.ordinal }
                    SortOrder.IN_PROGRESS -> filtered.filter {
                        it.progress != null && !it.progress.isCompleted && it.progress.completionPercent > 0
                    }
                }

                _uiState.value.copy(puzzles = filtered, isLoading = false)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setCategory(category: Category?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = order)
    }

    fun getTimeUntilNextDaily(): String {
        val now = LocalDate.now()
        val tomorrow = now.plusDays(1)
        val midnight = java.time.LocalDateTime.of(tomorrow, java.time.LocalTime.MIDNIGHT)
        val current = java.time.LocalDateTime.now()
        val hours = ChronoUnit.HOURS.between(current, midnight)
        val minutes = ChronoUnit.MINUTES.between(current, midnight) % 60
        val seconds = ChronoUnit.SECONDS.between(current, midnight) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
