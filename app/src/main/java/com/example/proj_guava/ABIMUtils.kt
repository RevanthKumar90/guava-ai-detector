package com.example.proj_guava

import android.graphics.*

object ABIMUtils {

    data class ABIMResult(
        val overlayBitmap: Bitmap,
        val infectionPercentage: Float
    )

    fun generateOverlay(bitmap: Bitmap, detectedDisease: String = ""): ABIMResult {

        val width = bitmap.width
        val height = bitmap.height

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val leafMask = Array(width) { BooleanArray(height) }
        val infectedMask = Array(width) { BooleanArray(height) }

        var leafPixels = 0
        val disease = detectedDisease.uppercase().trim()

        for (x in 0 until width) {
            for (y in 0 until height) {

                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel).toFloat()
                val g = Color.green(pixel).toFloat()
                val b = Color.blue(pixel).toFloat()
                val alpha = Color.alpha(pixel)

                if (alpha < 20) continue

                val hsv = FloatArray(3)
                Color.RGBToHSV(r.toInt(), g.toInt(), b.toInt(), hsv)
                val hue = hsv[0]
                val sat = hsv[1]
                val value = hsv[2]
                val brightness = (r + g + b) / 3f

                // BACKGROUND EXCLUSION
                val isDarkBg = value < 0.20f
                val isGrayBg = sat < 0.12f && brightness in 40f..160f
                val isSoilBg = hue in 10f..40f && sat > 0.3f && value < 0.45f && brightness < 120f
                val isWhiteBg = brightness > 245f

                if (isDarkBg || isGrayBg || isSoilBg || isWhiteBg) continue

                // LEAF DETECTION
                val isGreenLeaf = hue in 70f..160f && sat > 0.20f && value > 0.20f
                val isYellowLeaf = hue in 38f..72f && sat > 0.25f && value > 0.30f
                val isLightBrown = hue in 15f..45f && sat in 0.15f..0.60f && value in 0.35f..0.85f
                val isDriedTissue = sat < 0.25f && value > 0.55f && brightness > 140f && r > b

                val isLeaf = isGreenLeaf || isYellowLeaf || isLightBrown ||
                        (disease.contains("MUMMIF") && isDriedTissue)

                if (!isLeaf) continue

                leafPixels++
                leafMask[x][y] = true

                // INFECTION DETECTION
                val isInfected: Boolean = when {

                    disease.contains("MUMMIF") -> {
                        val isBrownDried = hue in 15f..45f && sat in 0.20f..0.65f && value > 0.35f
                        val isCreamDried = sat < 0.22f && value > 0.60f && r > g && r > b && brightness > 150f
                        val isDeadTissue = hue in 20f..55f && sat > 0.15f && value in 0.40f..0.90f
                        isBrownDried || isCreamDried || isDeadTissue
                    }

                    disease.contains("CANKER") -> {
                        val isDarkSpot = value < 0.38f && sat > 0.15f
                        val isBrownSpot = hue in 10f..40f && sat > 0.30f && value in 0.25f..0.65f
                        val isYellowHalo = hue in 40f..75f && sat > 0.35f && value > 0.45f
                        isDarkSpot || isBrownSpot || isYellowHalo
                    }

                    disease.contains("RUST") -> {
                        val isRustOrange = hue in 15f..40f && sat > 0.40f && value in 0.30f..0.80f
                        val isYellowSpot = hue in 40f..70f && sat > 0.35f && value > 0.40f
                        val isDarkPustule = value < 0.35f && sat > 0.20f
                        isRustOrange || isYellowSpot || isDarkPustule
                    }

                    disease.contains("DOT") -> {
                        val isDarkDot = value < 0.40f && sat > 0.15f
                        val isBrownCluster = hue in 10f..45f && sat > 0.25f && value in 0.20f..0.65f
                        val isYellowRing = hue in 45f..75f && sat > 0.30f && value > 0.40f
                        isDarkDot || isBrownCluster || isYellowRing
                    }

                    else -> {
                        val isBrownish = hue in 10f..50f && sat > 0.25f && value in 0.25f..0.75f
                        val isDarkArea = value < 0.35f && sat > 0.15f
                        val isYellowArea = hue in 40f..75f && sat > 0.30f
                        isBrownish || isDarkArea || isYellowArea
                    }
                }

                if (isInfected) {
                    infectedMask[x][y] = true
                }
            }
        }

        // SMOOTHING
        val smoothMask = Array(width) { BooleanArray(height) }

        for (x in 1 until width - 1) {
            for (y in 1 until height - 1) {

                if (!leafMask[x][y]) continue

                var infCount = 0
                for (dx in -1..1) {
                    for (dy in -1..1) {
                        if (dx == 0 && dy == 0) continue
                        if (isSafe(infectedMask, x + dx, y + dy)) infCount++
                    }
                }

                if (infectedMask[x][y] && infCount >= 1) {
                    smoothMask[x][y] = true
                } else if (!infectedMask[x][y] && infCount >= 6) {
                    smoothMask[x][y] = true
                }
            }
        }

        // COUNT
        var finalInfected = 0
        for (x in 1 until width - 1) {
            for (y in 1 until height - 1) {
                if (smoothMask[x][y]) finalInfected++
            }
        }

        // DRAW OVERLAY
        val fillPaint = Paint().apply {
            color = Color.argb(55, 255, 0, 0)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.argb(210, 255, 60, 60)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        val glowPaint = Paint().apply {
            color = Color.argb(80, 255, 0, 0)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            maskFilter = BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL)
            isAntiAlias = true
        }

        for (x in 1 until width - 1) {
            for (y in 1 until height - 1) {
                if (smoothMask[x][y]) {
                    canvas.drawPoint(x.toFloat(), y.toFloat(), fillPaint)
                    if (isEdgeSafe(smoothMask, x, y)) {
                        canvas.drawPoint(x.toFloat(), y.toFloat(), glowPaint)
                        canvas.drawPoint(x.toFloat(), y.toFloat(), borderPaint)
                    }
                }
            }
        }

        val percent = if (leafPixels > 0)
            (finalInfected.toFloat() / leafPixels) * 100f
        else 0f

        return ABIMResult(output, percent.coerceIn(0f, 100f))
    }

    private fun isSafe(mask: Array<BooleanArray>, x: Int, y: Int): Boolean {
        return x >= 0 && y >= 0 &&
                x < mask.size &&
                y < mask[0].size &&
                mask[x][y]
    }

    private fun isEdgeSafe(mask: Array<BooleanArray>, x: Int, y: Int): Boolean {
        for (dx in -1..1) {
            for (dy in -1..1) {
                val nx = x + dx
                val ny = y + dy
                if (nx < 0 || ny < 0 || nx >= mask.size || ny >= mask[0].size) return true
                if (!mask[nx][ny]) return true
            }
        }
        return false
    }
}