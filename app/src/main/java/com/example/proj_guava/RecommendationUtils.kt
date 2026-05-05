package com.example.proj_guava

object RecommendationUtils {

    fun getTreatment(disease: String): String {
        return when (disease) {

            "Canker" -> "Apply copper-based fungicide (like Copper Oxychloride) every 7–10 days. Prune and remove infected branches to prevent spread."

            "Rust" -> "Spray Mancozeb or Sulfur fungicide every 7 days. Remove severely infected leaves and avoid excess moisture."

            "Dot" -> "Use neem oil or Carbendazim spray weekly. Remove affected leaves early to stop disease progression."

            "Mummification" -> "Prune infected fruits and branches immediately. Dispose them away from field and apply fungicide to control spread."

            else -> "No major infection detected. Maintain plant health and monitor regularly."
        }
    }

    fun getPrevention(disease: String): String {
        return when (disease) {

            "Canker" -> "Avoid physical damage to plant. Use clean tools and maintain field hygiene to prevent bacterial entry."

            "Rust" -> "Ensure good air circulation and avoid overcrowding. Water plants at base to keep leaves dry."

            "Dot" -> "Avoid overwatering and maintain proper spacing. Regularly inspect leaves for early signs."

            "Mummification" -> "Practice regular pruning and remove fallen debris. Maintain proper sanitation in plantation."

            else -> "Follow general plant care practices like proper watering, sunlight, and periodic monitoring."
        }
    }
}