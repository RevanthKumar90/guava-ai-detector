package com.example.proj_guava

import android.graphics.Bitmap
import android.graphics.Color

object ImageUtils {

    fun isLeafImage(bitmap: Bitmap): Boolean {

        var greenPixels = 0
        var brownPixels = 0
        var sampledCount = 0

        for (x in 0 until bitmap.width step 10) {
            for (y in 0 until bitmap.height step 10) {

                val pixel = bitmap.getPixel(x, y)

                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                sampledCount++

                // Green check
                if (g > r && g > b && g > 60) {
                    greenPixels++
                }

                // Brown / Rust check
                if (r > g && g > b && r > 80 && g > 50) {
                    brownPixels++
                }
            }
        }

        if (sampledCount == 0) return true

        val greenRatio = greenPixels.toDouble() / sampledCount
        val brownRatio = brownPixels.toDouble() / sampledCount

        //  FINAL LOGIC
        return (greenRatio > 0.1 || brownRatio > 0.05)
    }

    fun isLowLight(bitmap: Bitmap): Boolean {

        var totalBrightness = 0
        var sampledCount = 0

        for (x in 0 until bitmap.width step 10) {
            for (y in 0 until bitmap.height step 10) {

                val pixel = bitmap.getPixel(x, y)

                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                val brightness = (r + g + b) / 3
                totalBrightness += brightness

                sampledCount++
            }
        }

        if (sampledCount == 0) return false

        val avgBrightness = totalBrightness / sampledCount

        return avgBrightness < 60
    }
}