package com.pixelcolor.app.data.generator

import com.pixelcolor.app.domain.model.Pixel

object PuzzleGenerator {

    fun generateSmiley(): List<Pixel> {
        val g = PixelArtGenerator(16, 16)
        val Y = 1 // yellow
        val B = 2 // black
        val R = 3 // red (mouth)
        val W = 4 // white (eyes)

        // Face fill
        g.circle(7, 7, 7, Y)

        // Left eye
        g.circle(5, 5, 1, B)
        g.circle(5, 5, 0, W)
        // Right eye
        g.circle(10, 5, 1, B)
        g.circle(10, 5, 0, W)

        // Mouth
        g.arc(7, 10, 4, 0, 180, R, 2)

        return g.build()
    }

    fun generateCat(): List<Pixel> {
        val g = PixelArtGenerator(32, 32)
        val O = 1 // orange body
        val W = 2 // white
        val B = 3 // black
        val P = 4 // pink
        val G = 5 // green eyes

        // Body
        g.ellipse(15, 20, 12, 10, O)

        // Head
        g.circle(15, 10, 8, O)

        // Ears
        g.triangle(7, 6, 5, 0, 11, 3, O)
        g.triangle(23, 6, 25, 0, 19, 3, O)
        g.triangle(8, 5, 6, 1, 10, 3, P)
        g.triangle(22, 5, 24, 1, 20, 3, P)

        // Eyes
        g.circle(11, 9, 2, W)
        g.circle(19, 9, 2, W)
        g.circle(11, 9, 1, G)
        g.circle(19, 9, 1, G)
        g.circle(11, 9, 0, B)
        g.circle(19, 9, 0, B)

        // Nose
        g.circle(15, 12, 1, P)

        // Mouth
        g.line(15, 13, 13, 15, B, 1)
        g.line(15, 13, 17, 15, B, 1)

        // Whiskers
        g.line(5, 12, 10, 11, B, 1)
        g.line(5, 13, 10, 13, B, 1)
        g.line(5, 14, 10, 14, B, 1)
        g.line(25, 12, 20, 11, B, 1)
        g.line(25, 13, 20, 13, B, 1)
        g.line(25, 14, 20, 14, B, 1)

        // Belly
        g.ellipse(15, 22, 6, 5, W)

        // Tail
        g.line(27, 22, 30, 12, O, 3)

        // Paws
        g.circle(10, 29, 2, W)
        g.circle(20, 29, 2, W)

        return g.build()
    }

    fun generateRocket(): List<Pixel> {
        val g = PixelArtGenerator(32, 32)
        val S = 1 // silver body
        val R = 2 // red
        val BL = 3 // blue windows
        val O = 4 // orange flame
        val Y = 5 // yellow flame
        val W = 6 // white window

        // Sky background with stars
        g.fill(0)

        // Body
        g.ellipse(15, 15, 5, 13, S)

        // Nose cone
        g.triangle(15, 0, 10, 8, 20, 8, R)

        // Window
        g.circle(15, 12, 3, BL)
        g.circle(15, 12, 2, W)
        g.circle(15, 12, 1, BL)

        // Window 2
        g.circle(15, 19, 2, W)

        // Fins
        g.triangle(10, 26, 6, 31, 12, 26, R)
        g.triangle(20, 26, 24, 31, 18, 26, R)

        // Stripe
        g.rect(11, 22, 19, 24, R)

        // Engine nozzle
        g.rect(12, 27, 18, 29, S)

        // Flame
        g.ellipse(15, 31, 4, 3, O)
        g.ellipse(15, 31, 2, 2, Y)

        // Stars
        for (sx in listOf(3, 7, 25, 28, 2, 29))
            for (sy in listOf(3, 8, 5, 10, 20, 15))
                if (sx in 0..31 && sy in 0..31 && g.let { true })
                    g.rect(sx, sy, sx, sy, W)

        return g.build()
    }

    fun generateSunset(): List<Pixel> {
        val g = PixelArtGenerator(48, 48)
        val SK1 = 1 // deep blue sky
        val SK2 = 2 // purple sky
        val SK3 = 3 // pink sky
        val SK4 = 4 // orange sky
        val SN = 5 // sun gold
        val MT = 6 // mountain dark green
        val TR = 7 // tree green
        val WT = 8 // water blue
        val GR = 9 // ground green
        val BR = 10 // brown

        // Sky gradient bands
        g.rect(0, 0, 47, 8, SK1)
        g.rect(0, 9, 47, 15, SK2)
        g.rect(0, 16, 47, 20, SK3)
        g.rect(0, 21, 47, 25, SK4)

        // Sun
        g.circle(24, 20, 5, SN)

        // Sun glow
        g.circle(24, 20, 7, SN).circle(24, 20, 6, SK4)

        // Mountains
        g.triangle(6, 30, 0, 26, 12, 26, MT)
        g.triangle(16, 30, 8, 22, 24, 22, MT)
        g.triangle(30, 30, 22, 24, 38, 24, MT)
        g.triangle(44, 30, 36, 26, 48, 26, MT)

        // Mountain snow caps
        g.triangle(16, 24, 14, 22, 18, 22, W = 11).let { it }
        g.triangle(30, 26, 28, 24, 32, 24, 11)

        // Water
        g.rect(0, 31, 47, 36, WT)

        // Water reflection
        for (rx in 10..38 step 2)
            g.rect(rx, 32, rx, 35, SK3)

        // Ground
        g.rect(0, 37, 47, 47, GR)

        // Trees
        for (tx in listOf(4, 12, 20, 28, 36, 44)) {
            g.rect(tx - 1, 34, tx + 1, 37, BR)
            g.triangle(tx, 30, tx - 4, 35, tx + 4, 35, TR)
            g.triangle(tx, 28, tx - 3, 33, tx + 3, 33, TR)
        }

        // Ground details
        g.rect(0, 44, 47, 47, BR)

        return g.build()
    }

    fun generateDragon(): List<Pixel> {
        val g = PixelArtGenerator(64, 64)
        val DR = 1 // dark red
        val OR = 2 // orange
        val GD = 3 // gold
        val BK = 4 // black
        val WH = 5 // white
        val GN = 6 // green
        val PU = 7 // purple
        val SL = 8 // silver horns
        val LR = 9 // light red

        // Background - dark sky
        g.fill(0)

        // Body - serpentine S-curve
        g.ellipse(20, 35, 10, 6, DR)
        g.ellipse(35, 30, 8, 5, DR)
        g.ellipse(48, 35, 7, 5, DR)

        // Body highlight
        g.ellipse(20, 33, 8, 3, OR)
        g.ellipse(35, 28, 6, 3, OR)
        g.ellipse(48, 33, 5, 3, OR)

        // Belly
        g.ellipse(20, 37, 6, 3, GD)
        g.ellipse(35, 32, 5, 2, GD)
        g.ellipse(48, 37, 4, 2, GD)

        // Neck
        g.ellipse(12, 28, 5, 8, DR)
        g.ellipse(12, 28, 3, 6, OR)

        // Head
        g.ellipse(10, 18, 7, 5, DR)
        g.ellipse(10, 18, 5, 3, OR)

        // Snout
        g.ellipse(5, 19, 4, 3, LR)

        // Eyes
        g.circle(8, 16, 2, WH)
        g.circle(8, 16, 1, GD)
        g.circle(8, 16, 0, BK)

        // Horns
        g.triangle(8, 12, 6, 6, 11, 10, SL)
        g.triangle(13, 12, 11, 6, 15, 10, SL)

        // Jaw
        g.ellipse(4, 21, 3, 2, DR)

        // Teeth
        g.rect(2, 20, 2, 22, WH)
        g.rect(5, 20, 5, 22, WH)

        // Left wing
        g.triangle(18, 22, 5, 5, 35, 10, PU)
        g.triangle(18, 22, 8, 8, 30, 12, PU)
        g.line(18, 22, 8, 8, LR, 2)
        g.line(18, 22, 18, 5, LR, 2)
        g.line(18, 22, 28, 8, LR, 2)

        // Right wing
        g.triangle(38, 20, 55, 5, 25, 10, PU)
        g.triangle(38, 20, 50, 8, 30, 12, PU)
        g.line(38, 20, 50, 8, LR, 2)
        g.line(38, 20, 40, 5, LR, 2)
        g.line(38, 20, 30, 8, LR, 2)

        // Tail
        val tailX = intArrayOf(55, 58, 60, 61, 60, 58, 56)
        val tailY = intArrayOf(35, 38, 42, 46, 50, 53, 55)
        for (i in tailX.indices) {
            g.circle(tailX[i], tailY[i], 3, DR)
            g.circle(tailX[i], tailY[i], 2, OR)
        }

        // Tail spikes
        for (i in tailX.indices step 2) {
            g.triangle(tailX[i], tailY[i] - 3, tailX[i] - 1, tailY[i] - 7, tailX[i] + 1, tailY[i] - 3, GD)
        }

        // Spine ridges along back
        val spineX = intArrayOf(15, 20, 25, 30, 35, 40, 45, 50)
        for (sx in spineX) {
            val bodyY = 28
            g.triangle(sx, bodyY - 4, sx - 2, bodyY - 9, sx + 2, bodyY - 4, GD)
        }

        // Claws
        for (cx in listOf(15, 18, 42, 45)) {
            g.circle(cx, 40, 2, DR)
            g.rect(cx - 1, 41, cx + 1, 43, SL)
        }

        // Fire breath
        for (i in 0..6) {
            g.circle(0 - i, 19, 3 - i / 3, when {
                i < 2 -> GD
                i < 4 -> OR
                else -> LR
            })
        }

        return g.build()
    }

    fun generateFlower(): List<Pixel> {
        val g = PixelArtGenerator(32, 32)
        val PK = 1 // pink petal
        val YL = 2 // yellow center
        val GR = 3 // green stem/leaf
        val DG = 4 // dark green
        val WH = 5 // white highlight

        // Stem
        g.rect(15, 18, 16, 31, GR)

        // Leaves
        g.ellipse(10, 26, 5, 2, GR)
        g.ellipse(22, 24, 5, 2, DG)

        // Petals - 8 petals around center
        val cx = 16
        val cy = 12
        val petalR = 6
        for (i in 0 until 8) {
            val angle = Math.PI * 2 * i / 8
            val px = (cx + petalR * kotlin.math.cos(angle)).toInt()
            val py = (cy + petalR * kotlin.math.sin(angle)).toInt()
            g.circle(px, py, 4, PK)
        }

        // Center
        g.circle(cx, cy, 3, YL)
        g.circle(cx, cy, 1, WH)

        // Petal highlights
        for (i in 0 until 8) {
            val angle = Math.PI * 2 * i / 8
            val px = (cx + (petalR - 1) * kotlin.math.cos(angle)).toInt()
            val py = (cy + (petalR - 1) * kotlin.math.sin(angle)).toInt()
            g.circle(px, py, 1, WH)
        }

        return g.build()
    }

    fun generateHeart(): List<Pixel> {
        val g = PixelArtGenerator(32, 32)
        val RD = 1 // red
        val DR = 2 // dark red
        val WH = 3 // white shine
        val PK = 4 // pink

        // Heart shape using two circles + triangle
        g.circle(11, 10, 7, RD)
        g.circle(21, 10, 7, RD)
        g.triangle(4, 13, 28, 13, 16, 28, RD)

        // Shading
        g.circle(11, 10, 5, DR)
        g.circle(21, 10, 5, DR)
        g.triangle(6, 15, 26, 15, 16, 26, DR)

        // Shine
        g.circle(9, 8, 2, WH)
        g.circle(19, 8, 2, WH)

        // Pink accents
        g.circle(10, 9, 1, PK)
        g.circle(20, 9, 1, PK)

        return g.build()
    }

    fun generateSpaceship(): List<Pixel> {
        val g = PixelArtGenerator(48, 48)
        val SL = 1 // silver
        val BL = 2 // blue
        val RD = 3 // red
        val YL = 4 // yellow
        val GN = 5 // green
        val WH = 6 // white
        val OR = 7 // orange
        val BK = 8 // black

        // Dark space background
        g.fill(0)

        // Stars
        for (i in 0 until 30) {
            val sx = (i * 17 + 3) % 48
            val sy = (i * 13 + 7) % 48
            g.rect(sx, sy, sx, sy, WH)
        }

        // Main hull
        g.ellipse(24, 28, 14, 6, SL)

        // Cockpit dome
        g.ellipse(24, 22, 6, 5, BL)
        g.ellipse(24, 21, 4, 3, WH)

        // Engine pods
        g.rect(8, 26, 12, 30, SL)
        g.rect(36, 26, 40, 30, SL)

        // Engine glow
        g.circle(10, 32, 2, OR)
        g.circle(38, 32, 2, OR)
        g.circle(10, 32, 1, YL)
        g.circle(38, 32, 1, YL)

        // Wings
        g.triangle(14, 28, 0, 20, 14, 22, SL)
        g.triangle(34, 28, 48, 20, 34, 22, SL)
        g.line(14, 26, 4, 22, RD, 2)
        g.line(34, 26, 44, 22, RD, 2)

        // Lights
        g.circle(18, 30, 1, RD)
        g.circle(24, 30, 1, GN)
        g.circle(30, 30, 1, RD)

        // Bottom detail
        g.rect(20, 33, 28, 35, BL)

        return g.build()
    }

    fun generateCactus(): List<Pixel> {
        val g = PixelArtGenerator(32, 32)
        val GR = 1 // green
        val DG = 2 // dark green
        val BR = 3 // brown pot
        val RD = 4 // red flower
        val YL = 5 // yellow
        val SD = 6 // sand

        // Sand ground
        g.rect(0, 27, 31, 31, SD)

        // Pot
        g.rect(11, 24, 21, 30, BR)
        g.rect(10, 23, 22, 25, BR)

        // Main stem
        g.rect(14, 6, 18, 24, GR)
        g.rect(15, 6, 17, 24, DG)

        // Left arm
        g.rect(8, 12, 14, 14, GR)
        g.rect(8, 8, 10, 12, GR)
        g.rect(9, 8, 9, 12, DG)

        // Right arm
        g.rect(18, 16, 24, 18, GR)
        g.rect(22, 12, 24, 18, GR)
        g.rect(23, 12, 23, 18, DG)

        // Flower on top
        g.circle(16, 5, 2, RD)
        g.circle(16, 5, 1, YL)

        // Spines
        for (y in 8..22 step 3) {
            g.rect(13, y, 13, y, YL)
            g.rect(19, y, 19, y, YL)
        }

        return g.build()
    }

    fun generateCastle(): List<Pixel> {
        val g = PixelArtGenerator(48, 48)
        val ST = 1 // stone gray
        val DK = 2 // dark stone
        val RD = 3 // red flag
        val BR = 4 // brown door
        val BL = 5 // blue sky
        val GR = 6 // green grass
        val YL = 7 // yellow window

        // Sky
        g.rect(0, 0, 47, 30, BL)

        // Grass
        g.rect(0, 35, 47, 47, GR)

        // Main wall
        g.rect(10, 20, 38, 40, ST)

        // Towers
        g.rect(8, 10, 16, 40, ST)
        g.rect(32, 10, 40, 40, ST)

        // Tower tops (crenellations)
        for (tx in listOf(8, 12, 16)) {
            g.rect(tx, 8, tx + 2, 10, ST)
            g.rect(tx, 8, tx + 1, 9, DK)
        }
        for (tx in listOf(32, 36, 40)) {
            g.rect(tx - 2, 8, tx, 10, ST)
            g.rect(tx - 1, 8, tx, 9, DK)
        }

        // Middle crenellations
        for (tx in 14..36 step 4) {
            g.rect(tx, 18, tx + 2, 20, ST)
        }

        // Door
        g.circle(24, 36, 4, BR)
        g.rect(20, 36, 28, 40, BR)

        // Windows
        g.circle(12, 18, 2, YL)
        g.circle(36, 18, 2, YL)
        g.rect(22, 24, 26, 28, YL)

        // Flags
        g.line(12, 5, 12, 10, DK, 2)
        g.line(36, 5, 36, 10, DK, 2)
        g.rect(12, 5, 17, 8, RD)
        g.rect(36, 5, 41, 8, RD)

        // Stone detail
        for (y in 22..38 step 3) {
            for (x in 12..36 step 4) {
                g.rect(x, y, x + 1, y, DK)
            }
        }

        return g.build()
    }

    fun generatePizza(): List<Pixel> {
        val g = PixelArtGenerator(32, 32)
        val CR = 1 // crust
        val RD = 2 // sauce red
        val YL = 3 // cheese yellow
        val PP = 4 // pepperoni
        val GN = 5 // green pepper
        val BR = 6 // mushroom brown

        // Crust triangle
        g.triangle(16, 2, 4, 28, 28, 28, CR)

        // Sauce layer
        g.triangle(16, 5, 7, 26, 25, 26, RD)

        // Cheese
        g.triangle(16, 7, 9, 24, 23, 24, YL)

        // Pepperoni
        g.circle(14, 14, 2, PP)
        g.circle(20, 18, 2, PP)
        g.circle(12, 22, 2, PP)
        g.circle(18, 12, 2, PP)

        // Green peppers
        g.ellipse(22, 14, 2, 1, GN)
        g.ellipse(16, 20, 2, 1, GN)

        // Mushrooms
        g.circle(10, 16, 1, BR)
        g.circle(24, 20, 1, BR)

        return g.build()
    }

    fun generateIceCream(): List<Pixel> {
        val g = PixelArtGenerator(32, 32)
        val WN = 1 // waffle brown
        val PK = 2 // strawberry pink
        val BR = 3 // chocolate brown
        val VN = 4 // vanilla cream
        val CH = 5 // cherry red
        val GR = 6 // green stem

        // Cone
        g.triangle(16, 47, 10, 20, 22, 20, WN)

        // Cone pattern
        g.line(11, 22, 21, 22, BR, 1)
        g.line(12, 26, 20, 26, BR, 1)
        g.line(13, 30, 19, 30, BR, 1)
        g.line(14, 34, 18, 34, BR, 1)

        // Vanilla scoop (bottom)
        g.circle(16, 18, 6, VN)

        // Chocolate scoop (middle)
        g.circle(16, 12, 5, BR)

        // Strawberry scoop (top)
        g.circle(16, 7, 4, PK)

        // Cherry
        g.circle(16, 3, 2, CH)
        g.line(16, 1, 18, -1, GR, 1)

        // Sprinkles
        for ((px, py) in listOf(13 to 10, 19 to 10, 14 to 16, 18 to 16, 12 to 14, 20 to 14)) {
            g.rect(px, py, px, py, PK)
        }

        return g.build()
    }
}
