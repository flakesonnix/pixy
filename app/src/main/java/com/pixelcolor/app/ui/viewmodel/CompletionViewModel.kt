package com.pixelcolor.app.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pixelcolor.app.PixelColorApplication
import com.pixelcolor.app.data.repository.PuzzleRepository
import com.pixelcolor.app.data.timelapse.TimeLapseRecorder
import com.pixelcolor.app.domain.model.PixelPuzzle
import com.pixelcolor.app.domain.model.UserProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class CompletionUiState(
    val puzzle: PixelPuzzle? = null,
    val progress: UserProgress? = null,
    val timelapseBitmaps: List<Bitmap> = emptyList(),
    val timelapseUri: android.net.Uri? = null,
    val isSavingTimelapse: Boolean = false,
    val isLoading: Boolean = true
)

class CompletionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: PuzzleRepository = (application as PixelColorApplication).puzzleRepository
    private val recorder = TimeLapseRecorder(application)

    private val _uiState = MutableStateFlow(CompletionUiState())
    val uiState: StateFlow<CompletionUiState> = _uiState.asStateFlow()

    fun loadPuzzle(puzzleId: String) {
        viewModelScope.launch {
            val puzzle = repo.getPuzzleById(puzzleId)
            val progress = repo.getProgressSync(puzzleId)
            _uiState.value = CompletionUiState(
                puzzle = puzzle,
                progress = progress,
                isLoading = false
            )
        }
    }

    fun generateTimelapsePreview(snapshots: List<List<com.pixelcolor.app.domain.model.FilledPixelState>>) {
        viewModelScope.launch {
            val puzzle = _uiState.value.puzzle ?: return@launch
            val bitmaps = recorder.renderPreviewBitmaps(snapshots, puzzle.gridWidth, puzzle.gridHeight)
            _uiState.value = _uiState.value.copy(timelapseBitmaps = bitmaps)
        }
    }

    fun saveTimelapseVideo(snapshots: List<List<com.pixelcolor.app.domain.model.FilledPixelState>>) {
        viewModelScope.launch {
            val puzzle = _uiState.value.puzzle ?: return@launch
            _uiState.value = _uiState.value.copy(isSavingTimelapse = true)

            val uri = recorder.encodeFramesToMp4(
                snapshots = snapshots,
                gridWidth = puzzle.gridWidth,
                gridHeight = puzzle.gridHeight,
                outputName = "pixelcolor_${puzzle.id}_${System.currentTimeMillis()}"
            )

            _uiState.value = _uiState.value.copy(
                timelapseUri = uri,
                isSavingTimelapse = false
            )
        }
    }

    fun shareImage() {
        val puzzle = _uiState.value.puzzle ?: return
        val progress = _uiState.value.progress ?: return

        viewModelScope.launch {
            try {
                // Render final image
                val snapshots = listOf(
                    progress.filledPixels.filter { it.value }.keys.map { key ->
                        val parts = key.split(",")
                        val x = parts[0].toInt()
                        val y = parts[1].toInt()
                        val pixel = puzzle.getPixelAt(x, y)
                        val colorHex = puzzle.getPaletteColor(pixel?.colorId ?: 0)?.hexColor ?: "#CCCCCC"
                        com.pixelcolor.app.domain.model.FilledPixelState(x, y, colorHex, 0)
                    }
                )

                val bitmaps = recorder.renderPreviewBitmaps(snapshots, puzzle.gridWidth, puzzle.gridHeight, 1024)
                if (bitmaps.isNotEmpty()) {
                    val bitmap = bitmaps.first()
                    val file = File(getApplication<android.app.Application>().cacheDir, "share_${puzzle.id}.png")
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    bitmap.recycle()

                    val uri = FileProvider.getUriForFile(
                        getApplication(),
                        "${getApplication<android.app.Application>().packageName}.provider",
                        file
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "My PixelColor Art: ${puzzle.title}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(shareIntent, "Share your pixel art")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    getApplication<android.app.Application>().startActivity(chooser)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun shareTimelapse() {
        val uri = _uiState.value.timelapseUri ?: return
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share time-lapse")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<android.app.Application>().startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
