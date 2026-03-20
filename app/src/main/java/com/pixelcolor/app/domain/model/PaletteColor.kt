package com.pixelcolor.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PaletteColor(
    val id: Int,
    val hexColor: String,
    val label: String
) {
    val longColor: Long
        get() = try {
            val cleaned = hexColor.removePrefix("#")
            when (cleaned.length) {
                6 -> 0xFF000000L or cleaned.toLong(16)
                8 -> cleaned.toLong(16)
                else -> 0xFF000000L
            }
        } catch (e: Exception) {
            0xFF000000L
        }
}
