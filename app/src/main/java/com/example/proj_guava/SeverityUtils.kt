package com.example.proj_guava

object SeverityUtils {

    //  Severity based on infection %
    fun getSeverity(infectionPercent: Float): String {
        return when {
            infectionPercent >= 70f -> "HIGH"
            infectionPercent >= 40f -> "MEDIUM"
            else -> "LOW"
        }
    }

    // Progress bar value (safe 0–100)
    fun getSeverityValue(infectionPercent: Float): Int {
        return infectionPercent
            .coerceIn(0f, 100f)
            .toInt()
    }

    // Display text (clean format)
    fun formatSeverity(severity: String): String {
        return severity.uppercase()
    }

    // Color (case-safe, improved shades)
    fun getSeverityColor(severity: String): Int {
        return when (severity.uppercase()) {
            "HIGH" -> 0xFFD32F2F.toInt()    // Strong Red
            "MEDIUM" -> 0xFFF57C00.toInt()  // Deep Orange
            else -> 0xFF2E7D32.toInt()      // Dark Green
        }
    }

    //  Optional: severity description (NEW - useful for UI)
    fun getSeverityDescription(severity: String): String {
        return when (severity.uppercase()) {
            "HIGH" -> "Severe infection detected. Immediate action required."
            "MEDIUM" -> "Moderate infection. Treatment recommended soon."
            else -> "Low infection. Monitor regularly."
        }
    }

    //  Combined helper (extended)
    fun getSeverityWithColor(infectionPercent: Float): Pair<String, Int> {
        val severity = getSeverity(infectionPercent)
        val color = getSeverityColor(severity)
        return Pair(severity, color)
    }
}