package com.pixelcolor.app.data.timelapse

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.pixelcolor.app.domain.model.FilledPixelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class TimeLapseRecorder(private val context: Context) {

    companion object {
        private const val MIME_TYPE = "video/avc"
        private const val FRAME_RATE = 12
        private const val BIT_RATE = 2_000_000
        private const val I_FRAME_INTERVAL = 1
        private const val VIDEO_SIZE = 512
    }

    suspend fun encodeFramesToMp4(
        snapshots: List<List<FilledPixelState>>,
        gridWidth: Int,
        gridHeight: Int,
        outputName: String
    ): Uri? = withContext(Dispatchers.IO) {
        if (snapshots.isEmpty()) return@withContext null

        val outputFile = createOutputFile(outputName) ?: return@withContext null

        try {
            val format = MediaFormat.createVideoFormat(MIME_TYPE, VIDEO_SIZE, VIDEO_SIZE).apply {
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                )
                setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
                setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
            }

            val encoder = MediaCodec.createEncoderByType(MIME_TYPE)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = encoder.createInputSurface()
            encoder.start()

            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false

            val canvas = inputSurface.lockCanvas(null)
            val bitmap = Bitmap.createBitmap(VIDEO_SIZE, VIDEO_SIZE, Bitmap.Config.ARGB_8888)
            val bitmapCanvas = Canvas(bitmap)

            val bgPaint = Paint().apply { color = AndroidColor.WHITE }
            val gridPaint = Paint().apply {
                color = AndroidColor.rgb(0xD0, 0xD0, 0xD0)
                strokeWidth = 0.5f
                style = Paint.Style.STROKE
            }

            val pixelSize = VIDEO_SIZE.toFloat() / gridWidth.coerceAtLeast(gridHeight)
            val offsetX = (VIDEO_SIZE - pixelSize * gridWidth) / 2f
            val offsetY = (VIDEO_SIZE - pixelSize * gridHeight) / 2f

            val frameDurationUs = 1_000_000L / FRAME_RATE
            val totalFrames = snapshots.size
            // Pad to at least 3 seconds
            val minFrames = FRAME_RATE * 3
            val frameStep = if (totalFrames > minFrames) totalFrames / minFrames else 1

            var presentationTimeUs = 0L
            var frameCount = 0

            val sampledIndices = if (totalFrames <= minFrames) {
                snapshots.indices.toList()
            } else {
                (0 until minFrames).map { (it * totalFrames) / minFrames }.distinct()
            }

            for (sampleIdx in sampledIndices) {
                val snapshot = snapshots[sampleIdx]

                // Clear
                bitmapCanvas.drawRect(0f, 0f, VIDEO_SIZE.toFloat(), VIDEO_SIZE.toFloat(), bgPaint)

                // Draw grid
                for (gx in 0..gridWidth) {
                    bitmapCanvas.drawLine(
                        offsetX + gx * pixelSize, offsetY,
                        offsetX + gx * pixelSize, offsetY + gridHeight * pixelSize,
                        gridPaint
                    )
                }
                for (gy in 0..gridHeight) {
                    bitmapCanvas.drawLine(
                        offsetX, offsetY + gy * pixelSize,
                        offsetX + gridWidth * pixelSize, offsetY + gy * pixelSize,
                        gridPaint
                    )
                }

                // Draw filled pixels
                val paint = Paint()
                for (pixel in snapshot) {
                    paint.color = try {
                        AndroidColor.parseColor(pixel.colorHex)
                    } catch (e: Exception) {
                        AndroidColor.GRAY
                    }
                    bitmapCanvas.drawRect(
                        offsetX + pixel.x * pixelSize,
                        offsetY + pixel.y * pixelSize,
                        offsetX + (pixel.x + 1) * pixelSize,
                        offsetY + (pixel.y + 1) * pixelSize,
                        paint
                    )
                }

                // Draw to surface
                canvas.drawBitmap(bitmap, 0f, 0f, null)

                // Drain encoder
                val bufferInfo = MediaCodec.BufferInfo()
                while (true) {
                    val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
                    if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) break
                    if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (muxerStarted) throw RuntimeException("Format changed twice")
                        trackIndex = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    } else if (outputBufferIndex >= 0) {
                        val encodedData = encoder.getOutputBuffer(outputBufferIndex) ?: continue
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            bufferInfo.size = 0
                        }
                        if (bufferInfo.size > 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                        }
                        encoder.releaseOutputBuffer(outputBufferIndex, false)
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
                    }
                }

                presentationTimeUs += frameDurationUs
                frameCount++
            }

            // Signal end of stream
            encoder.signalEndOfInputStream()

            // Final drain
            val bufferInfo = MediaCodec.BufferInfo()
            while (true) {
                val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 100_000)
                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) break
                if (outputBufferIndex >= 0) {
                    val encodedData = encoder.getOutputBuffer(outputBufferIndex) ?: continue
                    if (bufferInfo.size > 0 && muxerStarted) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outputBufferIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
                }
            }

            encoder.stop()
            encoder.release()
            if (muxerStarted) {
                muxer.stop()
            }
            muxer.release()
            bitmap.recycle()

            getFileUri(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            outputFile.delete()
            null
        }
    }

    private fun createOutputFile(name: String): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For API 29+, we'll save to app's external files
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            if (dir != null && !dir.exists()) dir.mkdirs()
            File(dir, "$name.mp4")
        } else {
            @Suppress("DEPRECATION")
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            if (!dir.exists()) dir.mkdirs()
            File(dir, "PixelColor/$name.mp4")
        }
    }

    private fun getFileUri(file: File): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/PixelColor")
            }
            try {
                val uri = context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
                )
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { os ->
                        file.inputStream().use { it.copyTo(os) }
                    }
                    file.delete()
                    it
                }
            } catch (e: Exception) {
                Uri.fromFile(file)
            }
        } else {
            Uri.fromFile(file)
        }
    }

    suspend fun renderPreviewBitmaps(
        snapshots: List<List<FilledPixelState>>,
        gridWidth: Int,
        gridHeight: Int,
        size: Int = 256
    ): List<Bitmap> = withContext(Dispatchers.Default) {
        if (snapshots.isEmpty()) return@withContext emptyList()

        val step = (snapshots.size / 30).coerceAtLeast(1)
        val sampled = snapshots.indices.step(step).map { snapshots[it] }

        sampled.map { snapshot ->
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(AndroidColor.WHITE)

            val pixelSize = size.toFloat() / gridWidth.coerceAtLeast(gridHeight)
            val offsetX = (size - pixelSize * gridWidth) / 2f
            val offsetY = (size - pixelSize * gridHeight) / 2f

            val paint = Paint()
            for (pixel in snapshot) {
                paint.color = try {
                    AndroidColor.parseColor(pixel.colorHex)
                } catch (e: Exception) {
                    AndroidColor.GRAY
                }
                canvas.drawRect(
                    offsetX + pixel.x * pixelSize,
                    offsetY + pixel.y * pixelSize,
                    offsetX + (pixel.x + 1) * pixelSize,
                    offsetY + (pixel.y + 1) * pixelSize,
                    paint
                )
            }
            bitmap
        }
    }
}
