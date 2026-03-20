package com.pixelcolor.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FilledPixelState(
    val x: Int,
    val y: Int,
    val colorHex: String,
    val timestampMs: Long
)
