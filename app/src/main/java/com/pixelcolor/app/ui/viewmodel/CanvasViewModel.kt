package com.pixelcolor.app.ui.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pixelcolor.app.PixelColorApplication
import com.pixelcolor.app.data.repository.PuzzleRepository
import com.pixelcolor.app.domain.model.FilledPixelState
import com.pixelcolor.app.domain.model.PixelPuzzle
import com.pixelcolor.app.domain.model.UserProgress
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CanvasUiState(
    val puzzle: PixelPuzzle? = null,
    val progress: UserProgress? = null,
    val selectedColorId: Int = 1,
    val zoom: Float = 1f,
    val panOffset: Offset = Offset.Zero,
    val elapsedMs: Long = 0L,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val hintsRemaining: Int = 3,
    val showHint: Boolean = false,
    val showSaveDialog: Boolean = false,
    val shakePixel: Pair<Int, Int>? = null,
    val isLoading: Boolean = true
)

class CanvasViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: PuzzleRepository = (application as PixelColorApplication).puzzleRepository

    private val _uiState = MutableStateFlow(CanvasUiState())
    val uiState: StateFlow<CanvasUiState> = _uiState.asStateFlow()

    // Time-lapse snapshots
    private val snapshots = mutableListOf<List<FilledPixelState>>()
    private var lastSnapshotCount = 0

    private var timerJob: kotlinx.coroutines.Job? = null
    private var autoSaveCounter = 0

    fun loadPuzzle(puzzleId: String) {
        viewModelScope.launch {
            val puzzle = repo.getPuzzleById(puzzleId)
            if (puzzle == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }

            val savedProgress = repo.getProgressSync(puzzleId)
            val progress = savedProgress ?: UserProgress(puzzleId = puzzleId)
            val filledCount = progress.filledPixels.count { it.value }
            val maxHints = 3
            val hintsUsed = progress.hintsUsed

            _uiState.value = _uiState.value.copy(
                puzzle = puzzle,
                progress = progress,
                selectedColorId = puzzle.palette.firstOrNull()?.id ?: 1,
                elapsedMs = progress.timeSpentMs,
                hintsRemaining = (maxHints - hintsUsed).coerceAtLeast(0),
                isCompleted = progress.isCompleted,
                isLoading = false
            )

            // Take initial snapshot
            snapshots.clear()
            captureSnapshot()

            // Start timer
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isPaused && !_uiState.value.isCompleted) {
                    _uiState.value = _uiState.value.copy(
                        elapsedMs = _uiState.value.elapsedMs + 1000
                    )
                }
            }
        }
    }

    fun selectColor(colorId: Int) {
        _uiState.value = _uiState.value.copy(selectedColorId = colorId, showHint = false)
    }

    fun tapPixel(x: Int, y: Int): Boolean {
        val state = _uiState.value
        val puzzle = state.puzzle ?: return false
        val progress = state.progress ?: return false

        val pixel = puzzle.getPixelAt(x, y) ?: return false
        val posKey = "$x,$y"

        if (progress.filledPixels[posKey] == true) return false

        // Check if color matches
        if (pixel.colorId != state.selectedColorId) {
            // Wrong color - shake
            _uiState.value = state.copy(shakePixel = x to y)
            viewModelScope.launch {
                delay(300)
                _uiState.value = _uiState.value.copy(shakePixel = null)
            }
            // Track attempt
            val updatedProgress = progress.copy(
                totalAttempts = progress.totalAttempts + 1
            )
            _uiState.value = _uiState.value.copy(progress = updatedProgress)
            return false
        }

        // Correct color - fill pixel
        val newFilled = progress.filledPixels + (posKey to true)
        val newCount = newFilled.count { it.value }
        val completionPercent = newCount.toFloat() / puzzle.totalPixels
        val isCompleted = completionPercent >= 1f

        val updatedProgress = progress.copy(
            filledPixels = newFilled,
            completionPercent = completionPercent,
            timeSpentMs = state.elapsedMs,
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else null,
            totalAttempts = progress.totalAttempts + 1,
            correctAttempts = progress.correctAttempts + 1
        )

        _uiState.value = state.copy(
            progress = updatedProgress,
            isCompleted = isCompleted
        )

        // Capture snapshot every 5 fills
        autoSaveCounter++
        if (autoSaveCounter % 5 == 0) {
            captureSnapshot()
        }

        // Auto-save every 10 fills
        if (autoSaveCounter % 10 == 0) {
            autoSave()
        }

        return true
    }

    private fun captureSnapshot() {
        val state = _uiState.value
        val puzzle = state.puzzle ?: return
        val progress = state.progress ?: return
        val palette = puzzle.palette.associateBy { it.id }

        val filled = progress.filledPixels.filter { it.value }.keys.map { key ->
            val parts = key.split(",")
            val x = parts[0].toInt()
            val y = parts[1].toInt()
            val pixel = puzzle.getPixelAt(x, y)
            val colorHex = palette[pixel?.colorId]?.hexColor ?: "#CCCCCC"
            FilledPixelState(x, y, colorHex, System.currentTimeMillis())
        }

        if (filled.size > lastSnapshotCount) {
            snapshots.add(filled)
            lastSnapshotCount = filled.size
        }
    }

    fun getSnapshots(): List<List<FilledPixelState>> = snapshots.toList()

    fun setZoom(zoom: Float) {
        _uiState.value = _uiState.value.copy(
            zoom = zoom.coerceIn(0.5f, 8f)
        )
    }

    fun setPan(offset: Offset) {
        _uiState.value = _uiState.value.copy(panOffset = offset)
    }

    fun togglePause() {
        _uiState.value = _uiState.value.copy(isPaused = !_uiState.value.isPaused)
    }

    fun useHint() {
        val state = _uiState.value
        if (state.hintsRemaining <= 0) return

        _uiState.value = state.copy(
            hintsRemaining = state.hintsRemaining - 1,
            showHint = true
        )

        viewModelScope.launch {
            delay(1500)
            _uiState.value = _uiState.value.copy(showHint = false)
        }

        // Update progress
        val progress = state.progress ?: return
        _uiState.value = _uiState.value.copy(
            progress = progress.copy(hintsUsed = progress.hintsUsed + 1)
        )
    }

    fun showSaveDialog() {
        _uiState.value = _uiState.value.copy(showSaveDialog = true)
    }

    fun dismissSaveDialog() {
        _uiState.value = _uiState.value.copy(showSaveDialog = false)
    }

    fun saveAndExit() {
        autoSave()
        timerJob?.cancel()
    }

    private fun autoSave() {
        val state = _uiState.value
        val progress = state.progress ?: return
        viewModelScope.launch {
            repo.saveProgress(progress.copy(timeSpentMs = state.elapsedMs))
        }
    }

    fun getColorRemainingCount(colorId: Int): Int {
        val puzzle = _uiState.value.puzzle ?: return 0
        val progress = _uiState.value.progress ?: return puzzle.getColorCount(colorId)
        val pixels = puzzle.getPixelsByColorId(colorId)
        return pixels.count { progress.filledPixels["${it.x},${it.y}"] != true }
    }

    fun getPixelColorHex(colorId: Int): String {
        return _uiState.value.puzzle?.getPaletteColor(colorId)?.hexColor ?: "#CCCCCC"
    }

    override fun onCleared() {
        super.onCleared()
        autoSave()
        timerJob?.cancel()
    }
}
