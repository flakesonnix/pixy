package com.pixelcolor.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Pixel(
    val x: Int,
    val y: Int,
    val colorId: Int,
    val isFilled: Boolean = false
) {
    val positionKey: String get() = "$x,$y"
}
