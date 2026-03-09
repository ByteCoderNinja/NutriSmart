package com.timofte.nutrismart.features.nutrition.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

data class GenerationConfig(
    val responseMimeType: String? = null,
    val maxOutputTokens: Int? = null,
    val temperature: Double? = null
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Candidate(
    val content: Content?,
    val finishReason: String?
)

data class ShoppingListResponse(
    val categories: List<ShoppingCategory> = emptyList()
)

data class ShoppingCategory(
    val name: String,
    val items: List<String>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeeklyPlanDTO(
    val days: List<DailyPlanDTO>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DailyPlanDTO(
    val dayNumber: Int,
    val breakfast: MealDTO,
    val lunch: MealDTO,
    val dinner: MealDTO,
    val snack: MealDTO
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MealDTO(
    val name: String,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val quantityDetails: String
)