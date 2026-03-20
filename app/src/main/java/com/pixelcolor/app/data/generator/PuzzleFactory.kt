package com.pixelcolor.app.data.generator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Color as AndroidColor
import com.pixelcolor.app.domain.model.Category
import com.pixelcolor.app.domain.model.Difficulty
import com.pixelcolor.app.domain.model.PixelPuzzle

object PuzzleFactory {

    fun createPuzzle(
        id: String,
        title: String,
        gridW: Int,
        gridH: Int,
        difficulty: Difficulty,
        category: Category,
        colors: Int = 10,
        draw: Canvas.(w: Int, h: Int) -> Unit
    ): PixelPuzzle {
        val bitmap = Bitmap.createBitmap(gridW, gridH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(AndroidColor.WHITE)
        canvas.draw(gridW, gridH)
        val result = ImageToPixelArt.fromBitmap(bitmap, gridW, gridH, colors)
        bitmap.recycle()
        return PixelPuzzle(
            id = id,
            title = title,
            gridWidth = gridW,
            gridHeight = gridH,
            palette = result.palette,
            pixels = result.pixels,
            difficulty = difficulty,
            category = category
        )
    }

    // ---------- built-in puzzles ----------

    fun smiley() = createPuzzle("smiley_16x16", "Happy Smiley", 16, 16, Difficulty.EASY, Category.RETRO, 6) { w, h ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // face
        p.color = AndroidColor.rgb(255, 215, 0)
        drawCircle(w / 2f, h / 2f, w / 2.2f, p)
        // eyes
        p.color = AndroidColor.BLACK
        drawCircle(w * 0.35f, h * 0.38f, 1.5f, p)
        drawCircle(w * 0.65f, h * 0.38f, 1.5f, p)
        // mouth
        p.style = Paint.Style.STROKE; p.strokeWidth = 1f
        drawArc(RectF(w * 0.25f, h * 0.3f, w * 0.75f, h * 0.8f), 10f, 160f, false, p)
    }

    fun cat() = createPuzzle("pixel_cat_32x32", "Pixel Cat", 32, 32, Difficulty.EASY, Category.ANIMALS, 8) { w, h ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // body
        p.color = AndroidColor.rgb(255, 140, 0)
        drawOval(RectF(w * 0.1f, h * 0.35f, w * 0.9f, h * 0.85f), p)
        // head
        drawCircle(w / 2f, h * 0.25f, w * 0.25f, p)
        // ears
        val ear = Path()
        ear.moveTo(w * 0.2f, h * 0.18f); ear.lineTo(w * 0.15f, h * 0.02f); ear.lineTo(w * 0.35f, h * 0.12f); ear.close()
        drawPath(ear, p)
        ear.reset()
        ear.moveTo(w * 0.8f, h * 0.18f); ear.lineTo(w * 0.85f, h * 0.02f); ear.lineTo(w * 0.65f, h * 0.12f); ear.close()
        drawPath(ear, p)
        // eyes
        p.color = AndroidColor.rgb(50, 205, 50)
        drawCircle(w * 0.38f, h * 0.22f, 2.5f, p)
        drawCircle(w * 0.62f, h * 0.22f, 2.5f, p)
        p.color = AndroidColor.BLACK
        drawCircle(w * 0.38f, h * 0.22f, 1f, p)
        drawCircle(w * 0.62f, h * 0.22f, 1f, p)
        // nose
        p.color = AndroidColor.rgb(255, 182, 193)
        drawCircle(w / 2f, h * 0.3f, 1.5f, p)
        // belly
        p.color = AndroidColor.WHITE
        drawOval(RectF(w * 0.3f, h * 0.55f, w * 0.7f, h * 0.78f), p)
    }

    fun rocket() = createPuzzle("rocket_ship_32x32", "Rocket Ship", 32, 32, Difficulty.EASY, Category.SPACE, 8) { w, h ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // body
        p.color = AndroidColor.rgb(200, 200, 200)
        drawOval(RectF(w * 0.3f, h * 0.12f, w * 0.7f, h * 0.82f), p)
        // nose cone
        p.color = AndroidColor.RED
        val nose = Path()
        nose.moveTo(w * 0.5f, h * 0.0f)
        nose.lineTo(w * 0.3f, h * 0.2f)
        nose.lineTo(w * 0.7f, h * 0.2f)
        nose.close()
        drawPath(nose, p)
        // window
        p.color = AndroidColor.rgb(30, 144, 255)
        drawCircle(w / 2f, h * 0.35f, w * 0.1f, p)
        p.color = AndroidColor.WHITE
        drawCircle(w / 2f, h * 0.35f, w * 0.06f, p)
        // fins
        p.color = AndroidColor.RED
        val finL = Path()
        finL.moveTo(w * 0.3f, h * 0.7f); finL.lineTo(w * 0.1f, h * 0.95f); finL.lineTo(w * 0.35f, h * 0.8f); finL.close()
        drawPath(finL, p)
        val finR = Path()
        finR.moveTo(w * 0.7f, h * 0.7f); finR.lineTo(w * 0.9f, h * 0.95f); finR.lineTo(w * 0.65f, h * 0.8f); finR.close()
        drawPath(finR, p)
        // flame
        p.color = AndroidColor.rgb(255, 165, 0)
        drawOval(RectF(w * 0.35f, h * 0.82f, w * 0.65f, h * 0.98f), p)
        p.color = AndroidColor.YELLOW
        drawOval(RectF(w * 0.4f, h * 0.85f, w * 0.6f, h * 0.95f), p)
    }

    fun sunset() = createPuzzle("sunset_landscape_48x48", "Sunset Landscape", 48, 48, Difficulty.MEDIUM, Category.NATURE, 10) { w, h ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // sky
        val colors = intArrayOf(
            AndroidColor.rgb(25, 25, 112),
            AndroidColor.rgb(75, 0, 130),
            AndroidColor.rgb(255, 20, 147),
            AndroidColor.rgb(255, 140, 0),
            AndroidColor.rgb(255, 215, 0)
        )
        val bandH = h * 0.55f / colors.size
        for (i in colors.indices) {
            p.color = colors[i]
            drawRect(0f, i * bandH, w.toFloat(), (i + 1) * bandH, p)
        }
        // sun
        p.color = AndroidColor.rgb(255, 215, 0)
        drawCircle(w / 2f, h * 0.4f, w * 0.1f, p)
        // mountains
        p.color = AndroidColor.rgb(47, 79, 79)
        val m1 = Path(); m1.moveTo(0f, h * 0.6f); m1.lineTo(w * 0.2f, h * 0.38f); m1.lineTo(w * 0.4f, h * 0.55f); m1.lineTo(w * 0.55f, h * 0.35f); m1.lineTo(w * 0.7f, h * 0.5f); m1.lineTo(w * 0.85f, h * 0.4f); m1.lineTo(w.toFloat(), h * 0.55f); m1.lineTo(w.toFloat(), h * 0.6f); m1.close()
        drawPath(m1, p)
        // water
        p.color = AndroidColor.rgb(30, 144, 255)
        drawRect(0f, h * 0.6f, w.toFloat(), h * 0.72f, p)
        // ground
        p.color = AndroidColor.rgb(34, 139, 34)
        drawRect(0f, h * 0.72f, w.toFloat(), h.toFloat(), p)
        // trees
        p.color = AndroidColor.rgb(0, 100, 0)
        for (tx in listOf(6f, 18f, 30f, 42f)) {
            p.color = AndroidColor.rgb(139, 69, 19)
            drawRect(tx - 0.5f, h * 0.66f, tx + 0.5f, h * 0.72f, p)
            p.color = AndroidColor.rgb(0, 100, 0)
            drawCircle(tx, h * 0.62f, 3f, p)
        }
    }

    fun dragon() = createPuzzle("dragon_64x64", "Mighty Dragon", 64, 64, Difficulty.HARD, Category.FANTASY, 12) { w, h ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // background
        drawColor(AndroidColor.rgb(20, 20, 40))
        // stars
        p.color = AndroidColor.WHITE
        val rng = kotlin.random.Random(7)
        repeat(40) { drawPoint(rng.nextFloat() * w, rng.nextFloat() * h * 0.5f, p) }
        // body
        p.color = AndroidColor.rgb(139, 0, 0)
        drawOval(RectF(w * 0.15f, h * 0.4f, w * 0.85f, h * 0.7f), p)
        // belly
        p.color = AndroidColor.rgb(255, 215, 0)
        drawOval(RectF(w * 0.25f, h * 0.52f, w * 0.75f, h * 0.65f), p)
        // neck
        p.color = AndroidColor.rgb(139, 0, 0)
        drawOval(RectF(w * 0.1f, h * 0.25f, w * 0.3f, h * 0.5f), p)
        // head
        drawOval(RectF(w * 0.02f, h * 0.12f, w * 0.25f, h * 0.32f), p)
        // snout
        p.color = AndroidColor.rgb(255, 99, 71)
        drawOval(RectF(0f, h * 0.18f, w * 0.12f, h * 0.28f), p)
        // eye
        p.color = AndroidColor.rgb(255, 215, 0)
        drawCircle(w * 0.14f, h * 0.2f, 2.5f, p)
        p.color = AndroidColor.BLACK
        drawCircle(w * 0.14f, h * 0.2f, 1f, p)
        // horns
        p.color = AndroidColor.rgb(192, 192, 192)
        val hL = Path(); hL.moveTo(w * 0.13f, h * 0.14f); hL.lineTo(w * 0.08f, h * 0.02f); hL.lineTo(w * 0.2f, h * 0.1f); hL.close()
        drawPath(hL, p)
        val hR = Path(); hR.moveTo(w * 0.22f, h * 0.14f); hR.lineTo(w * 0.28f, h * 0.02f); hR.lineTo(w * 0.18f, h * 0.1f); hR.close()
        drawPath(hR, p)
        // left wing
        p.color = AndroidColor.rgb(75, 0, 130)
        val wL = Path(); wL.moveTo(w * 0.25f, h * 0.35f); wL.lineTo(w * 0.02f, h * 0.05f); wL.lineTo(w * 0.55f, h * 0.12f); wL.lineTo(w * 0.45f, h * 0.35f); wL.close()
        drawPath(wL, p)
        // right wing
        val wR = Path(); wR.moveTo(w * 0.65f, h * 0.35f); wR.lineTo(w * 0.95f, h * 0.08f); wR.lineTo(w * 0.4f, h * 0.15f); wR.lineTo(w * 0.55f, h * 0.35f); wR.close()
        drawPath(wR, p)
        // tail
        p.color = AndroidColor.rgb(139, 0, 0)
        val tail = Path()
        tail.moveTo(w * 0.8f, h * 0.55f)
        tail.cubicTo(w * 0.9f, h * 0.6f, w, h * 0.75f, w * 0.95f, h * 0.88f)
        tail.lineTo(w * 0.88f, h * 0.85f)
        tail.cubicTo(w * 0.92f, h * 0.72f, w * 0.85f, h * 0.58f, w * 0.78f, h * 0.55f)
        tail.close()
        drawPath(tail, p)
        // fire breath
        p.color = AndroidColor.rgb(255, 215, 0)
        drawOval(RectF(0f, h * 0.16f, w * 0.08f, h * 0.26f), p)
        p.color = AndroidColor.rgb(255, 140, 0)
        drawOval(RectF(0f, h * 0.18f, w * 0.04f, h * 0.24f), p)
    }

    fun heart() = createPuzzle("heart_32x32", "Love Heart", 32, 32, Difficulty.EASY, Category.RETRO, 5) { w, h ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = AndroidColor.rgb(220, 20, 60)
        drawCircle(w * 0.35f, h * 0.3f, w * 0.22f, p)
        drawCircle(w * 0.65f, h * 0.3f, w * 0.22f, p)
        val tri = Path()
        tri.moveTo(w * 0.13f, h * 0.38f)
        tri.lineTo(w * 0.87f, h * 0.38f)
        tri.lineTo(w / 2f, h * 0.9f)
        tri.close()
        drawPath(tri, p)
        // shine
        p.color = AndroidColor.rgb(255, 182, 193)
        drawCircle(w * 0.32f, h * 0.25f, 3f, p)
    }

    fun flower() = createPuzzle("flower_32x32", "Sunflower", 32, 32, Difficulty.EASY, Category.NATURE, 7) { w, h ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // stem
        p.color = AndroidColor.rgb(34, 139, 34)
        drawRect(w * 0.45f, h * 0.5f, w * 0.55f, h * 0.95f, p)
        // leaves
        val leaf = Path()
        leaf.moveTo(w * 0.5f, h * 0.7f); leaf.quadTo(w * 0.25f, h * 0.6f, w * 0.3f, h * 0.75f); leaf.close()
        drawPath(leaf, p)
        leaf.reset()
        leaf.moveTo(w * 0.5f, h * 0.65f); leaf.quadTo(w * 0.75f, h * 0.55f, w * 0.7f, h * 0.7f); leaf.close()
        drawPath(leaf, p)
        // petals
        p.color = AndroidColor.rgb(255, 215, 0)
        for (i in 0 until 8) {
            val angle = Math.PI * 2 * i / 8
            val px = w / 2f + w * 0.2f * kotlin.math.cos(angle).toFloat()
            val py = h * 0.32f + h * 0.2f * kotlin.math.sin(angle).toFloat()
            drawOval(RectF(px - 4, py - 6, px + 4, py + 6), p)
        }
        // center
        p.color = AndroidColor.rgb(139, 69, 19)
        drawCircle(w / 2f, h * 0.32f, w * 0.12f, p)
    }

    fun cactus() = createPuzzle("cactus_32x32", "Desert Cactus", 32, 32, Difficulty.EASY, Category.NATURE, 6) { w, h ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // sky
        drawColor(AndroidColor.rgb(135, 206, 235))
        // sand
        p.color = AndroidColor.rgb(210, 180, 140)
        drawRect(0f, h * 0.8f, w.toFloat(), h.toFloat(), p)
        // pot
        p.color = AndroidColor.rgb(139, 69, 19)
        drawRect(w * 0.3f, h * 0.75f, w * 0.7f, h * 0.92f, p)
        drawRect(w * 0.28f, h * 0.73f, w * 0.72f, h * 0.77f, p)
        // main stem
        p.color = AndroidColor.rgb(34, 139, 34)
        drawRect(w * 0.42f, h * 0.15f, w * 0.58f, h * 0.75f, p)
        // left arm
        drawRect(w * 0.2f, h * 0.35f, w * 0.44f, h * 0.42f, p)
        drawRect(w * 0.2f, h * 0.22f, w * 0.28f, h * 0.35f, p)
        // right arm
        drawRect(w * 0.56f, h * 0.45f, w * 0.8f, h * 0.52f, p)
        drawRect(w * 0.72f, h * 0.35f, w * 0.8f, h * 0.52f, p)
        // flower
        p.color = AndroidColor.RED
        drawCircle(w * 0.5f, h * 0.13f, 3f, p)
        p.color = AndroidColor.YELLOW
        drawCircle(w * 0.5f, h * 0.13f, 1.5f, p)
    }

    fun pizza() = createPuzzle("pizza_32x32", "Slice of Pizza", 32, 32, Difficulty.MEDIUM, Category.FOOD, 8) { w, h ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // background
        drawColor(AndroidColor.rgb(245, 245, 220))
        // crust
        p.color = AndroidColor.rgb(210, 180, 140)
        val slice = Path()
        slice.moveTo(w / 2f, h * 0.05f)
        slice.lineTo(w * 0.1f, h * 0.9f)
        slice.lineTo(w * 0.9f, h * 0.9f)
        slice.close()
        drawPath(slice, p)
        // sauce
        p.color = AndroidColor.rgb(200, 30, 30)
        val sauce = Path()
        sauce.moveTo(w / 2f, h * 0.12f)
        sauce.lineTo(w * 0.18f, h * 0.85f)
        sauce.lineTo(w * 0.82f, h * 0.85f)
        sauce.close()
        drawPath(sauce, p)
        // cheese
        p.color = AndroidColor.rgb(255, 223, 0)
        val cheese = Path()
        cheese.moveTo(w / 2f, h * 0.18f)
        cheese.lineTo(w * 0.24f, h * 0.8f)
        cheese.lineTo(w * 0.76f, h * 0.8f)
        cheese.close()
        drawPath(cheese, p)
        // pepperoni
        p.color = AndroidColor.rgb(180, 30, 30)
        drawCircle(w * 0.42f, h * 0.4f, 3f, p)
        drawCircle(w * 0.58f, h * 0.55f, 3f, p)
        drawCircle(w * 0.5f, h * 0.7f, 3f, p)
        drawCircle(w * 0.35f, h * 0.65f, 2.5f, p)
        // green pepper
        p.color = AndroidColor.rgb(34, 139, 34)
        drawOval(RectF(w * 0.55f, h * 0.35f, w * 0.65f, h * 0.4f), p)
    }

    fun iceCream() = createPuzzle("icecream_32x32", "Ice Cream Cone", 32, 32, Difficulty.MEDIUM, Category.FOOD, 7) { w, h ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        drawColor(AndroidColor.rgb(173, 216, 230))
        // cone
        p.color = AndroidColor.rgb(210, 180, 140)
        val cone = Path()
        cone.moveTo(w * 0.3f, h * 0.45f)
        cone.lineTo(w * 0.5f, h * 0.98f)
        cone.lineTo(w * 0.7f, h * 0.45f)
        cone.close()
        drawPath(cone, p)
        // waffle lines
        p.color = AndroidColor.rgb(160, 130, 90)
        p.style = Paint.Style.STROKE; p.strokeWidth = 0.5f
        for (i in 1..5) {
            val yy = h * (0.45f + 0.1f * i)
            drawLine(w * (0.3f + 0.02f * i), yy, w * (0.7f - 0.02f * i), yy, p)
        }
        p.style = Paint.Style.FILL
        // scoops
        p.color = AndroidColor.rgb(255, 182, 193) // strawberry
        drawCircle(w / 2f, h * 0.38f, w * 0.15f, p)
        p.color = AndroidColor.rgb(210, 180, 140) // vanilla
        drawCircle(w * 0.42f, h * 0.22f, w * 0.13f, p)
        p.color = AndroidColor.rgb(123, 63, 0) // chocolate
        drawCircle(w * 0.58f, h * 0.22f, w * 0.13f, p)
        // cherry
        p.color = AndroidColor.RED
        drawCircle(w / 2f, h * 0.08f, 3f, p)
        // sprinkles
        p.color = AndroidColor.rgb(255, 0, 255)
        drawRect(w * 0.4f, h * 0.18f, w * 0.43f, h * 0.2f, p)
        drawRect(w * 0.57f, h * 0.25f, w * 0.6f, h * 0.27f, p)
        p.color = AndroidColor.rgb(0, 200, 0)
        drawRect(w * 0.48f, h * 0.15f, w * 0.51f, h * 0.17f, p)
    }

    fun castle() = createPuzzle("castle_48x48", "Medieval Castle", 48, 48, Difficulty.MEDIUM, Category.FANTASY, 10) { w, h ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // sky
        drawColor(AndroidColor.rgb(100, 149, 237))
        // grass
        p.color = AndroidColor.rgb(34, 139, 34)
        drawRect(0f, h * 0.7f, w.toFloat(), h.toFloat(), p)
        // main wall
        p.color = AndroidColor.rgb(169, 169, 169)
        drawRect(w * 0.2f, h * 0.4f, w * 0.8f, h * 0.85f, p)
        // towers
        drawRect(w * 0.12f, h * 0.2f, w * 0.28f, h * 0.85f, p)
        drawRect(w * 0.72f, h * 0.2f, w * 0.88f, h * 0.85f, p)
        // crenellations
        p.color = AndroidColor.rgb(128, 128, 128)
        for (tx in listOf(0.12f, 0.18f, 0.24f)) {
            drawRect(w * tx, h * 0.16f, w * (tx + 0.03f), h * 0.2f, p)
        }
        for (tx in listOf(0.72f, 0.78f, 0.84f)) {
            drawRect(w * tx, h * 0.16f, w * (tx + 0.03f), h * 0.2f, p)
        }
        for (tx in 0.22f..0.78f step 0.06f) {
            drawRect(w * tx, h * 0.36f, w * (tx + 0.03f), h * 0.4f, p)
        }
        // door
        p.color = AndroidColor.rgb(139, 69, 19)
        drawRect(w * 0.42f, h * 0.62f, w * 0.58f, h * 0.85f, p)
        drawCircle(w * 0.5f, h * 0.62f, w * 0.08f, p)
        // windows
        p.color = AndroidColor.rgb(255, 215, 0)
        drawCircle(w * 0.2f, h * 0.35f, 2.5f, p)
        drawCircle(w * 0.8f, h * 0.35f, 2.5f, p)
        drawRect(w * 0.46f, h * 0.48f, w * 0.54f, h * 0.56f, p)
        // flags
        p.color = AndroidColor.RED
        drawRect(w * 0.19f, h * 0.05f, w * 0.21f, h * 0.2f, p)
        drawRect(w * 0.19f, h * 0.05f, w * 0.26f, h * 0.1f, p)
        drawRect(w * 0.79f, h * 0.05f, w * 0.81f, h * 0.2f, p)
        drawRect(w * 0.79f, h * 0.05f, w * 0.86f, h * 0.1f, p)
    }

    fun spaceship() = createPuzzle("spaceship_48x48", "UFO Explorer", 48, 48, Difficulty.MEDIUM, Category.SPACE, 8) { w, h ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // space background
        drawColor(AndroidColor.rgb(10, 10, 30))
        // stars
        p.color = AndroidColor.WHITE
        val rng = kotlin.random.Random(3)
        repeat(50) { drawPoint(rng.nextFloat() * w, rng.nextFloat() * h, p) }
        // saucer body
        p.color = AndroidColor.rgb(192, 192, 192)
        drawOval(RectF(w * 0.1f, h * 0.4f, w * 0.9f, h * 0.6f), p)
        // dome
        p.color = AndroidColor.rgb(100, 149, 237)
        drawOval(RectF(w * 0.3f, h * 0.2f, w * 0.7f, h * 0.5f), p)
        p.color = AndroidColor.rgb(173, 216, 230)
        drawOval(RectF(w * 0.35f, h * 0.25f, w * 0.65f, h * 0.45f), p)
        // lights
        p.color = AndroidColor.rgb(255, 0, 0)
        drawCircle(w * 0.25f, h * 0.5f, 2f, p)
        drawCircle(w * 0.5f, h * 0.55f, 2f, p)
        drawCircle(w * 0.75f, h * 0.5f, 2f, p)
        // beam
        p.color = AndroidColor.rgb(255, 255, 100)
        p.alpha = 80
        val beam = Path()
        beam.moveTo(w * 0.35f, h * 0.6f)
        beam.lineTo(w * 0.15f, h)
        beam.lineTo(w * 0.85f, h)
        beam.lineTo(w * 0.65f, h * 0.6f)
        beam.close()
        drawPath(beam, p)
    }

    fun allBundled(): List<PixelPuzzle> = listOf(
        smiley(), cat(), rocket(), heart(), flower(),
        cactus(), pizza(), iceCream(), sunset(), castle(), spaceship(), dragon()
    )
}
