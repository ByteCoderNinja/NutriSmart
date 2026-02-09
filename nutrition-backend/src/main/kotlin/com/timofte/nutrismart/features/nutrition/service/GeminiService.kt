package com.timofte.nutrismart.features.nutrition.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.timofte.nutrismart.features.nutrition.model.Content
import com.timofte.nutrismart.features.nutrition.model.GeminiRequest
import com.timofte.nutrismart.features.nutrition.model.GeminiResponse
import com.timofte.nutrismart.features.nutrition.model.Part
import com.timofte.nutrismart.features.nutrition.model.WeeklyPlanDTO
import com.timofte.nutrismart.features.user.model.UserEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class GeminiService(
    @Value("\${gemini.api.key}") private val apiKey: String,
) {
    private val restTemplate = RestTemplate()

    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    fun generateWeeklyPlan(user: UserEntity): WeeklyPlanDTO {

        val promptText = buildPrompt(user)

        val requestBody = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = promptText))))
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(requestBody, headers)

        val urlWithKey = "$apiUrl?key=$apiKey"

        try {
            val response = restTemplate.postForObject(urlWithKey, entity, GeminiResponse::class.java)

            val rawJson = response?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw RuntimeException("Gemini response contains null content")

            val cleanJson = cleanJsonString(rawJson)

            println("JSON CURAT: $cleanJson")

            return objectMapper.readValue(cleanJson, WeeklyPlanDTO::class.java)
        } catch (e: Exception) {
            println("Error: ${e.message}")
            throw e
        }
    }

    private fun cleanJsonString(rawText: String): String {
        val startIndex = rawText.indexOf('{')
        val endIndex = rawText.lastIndexOf('}')

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return rawText.substring(startIndex, endIndex + 1)
        }

        return rawText
    }

    private fun buildPrompt(user: UserEntity): String {
        return """
            IMPORTANT: Return ONLY the raw JSON. Do not include any Markdown formatting like \``json. Do not add any introductory text.
            Act as a professional nutritionist. Generate a 7-DAY meal plan (Monday to Sunday) for a user with the following profile:
            - Age: ${java.time.Period.between(user.dateOfBirth, java.time.LocalDate.now()).years}
            - Gender: ${user.gender}
            - Weight: ${user.weight} kg
            - Height: ${user.height} cm
            - Activity Level: ${user.activityLevel}
            - Goal: ${user.targetWeight} kg
            - Medical Conditions: ${user.medicalConditions}
            - Budget Constraint: ${user.maxDailyBudget} RON / day
            - Calories: ${user.targetCalories} kcal/day
            
            IMPORTANT RULES:
            1. Output strictly valid JSON.
            2. Structure MUST be exactly this (List of days):
            {
              "days": [
                {
                  "dayNumber": 1,
                  "breakfast": { "name": "...", "calories": 100, "protein": 10, "fat": 5, "carbs": 20, "quantityDetails": "..." },
                  "lunch": { "name": "...", "calories": 100, "protein": 10, "fat": 5, "carbs": 20, "quantityDetails": "..." },
                  "dinner": { "name": "...", "calories": 100, "protein": 10, "fat": 5, "carbs": 20, "quantityDetails": "..." },
                  "snack": { "name": "...", "calories": 100, "protein": 10, "fat": 5, "carbs": 20, "quantityDetails": "..." }
                },
                ... (repeat for all 7 days)
              ]
            }
            3. "quantityDetails" should be very descriptive (e.g., "2 medium eggs, 1 avocado").
            4. Optimize for low food waste (reuse ingredients between days where possible).
            5. Provide realistic macronutrients close to the target.
        """.trimIndent()
    }
}