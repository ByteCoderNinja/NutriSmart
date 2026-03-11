package com.timofte.nutrismart.features.nutrition.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.timofte.nutrismart.features.nutrition.model.*
import com.timofte.nutrismart.features.user.model.UserEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.Period

@Service
class GeminiService(
    @Value("\${gemini.api.key}") private val apiKey: String,
) {
    private val restTemplate = RestTemplate()

    private val objectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent"

    fun generateWeeklyPlan(user: UserEntity): WeeklyPlanDTO {
        val promptText = buildPlanPrompt(user)

        return callGemini(promptText, WeeklyPlanDTO::class.java)
            ?: throw RuntimeException("Error while generating Gemini weekly plan")
    }

    fun generateShoppingList(ingredients: List<String>, user: UserEntity): ShoppingListResponse {
        val promptText = buildShoppingListPrompt(ingredients, user)

        return callGemini(promptText, ShoppingListResponse::class.java)
            ?: ShoppingListResponse(emptyList())
    }

    private fun buildPlanPrompt(user: UserEntity): String {
        val age = Period.between(user.dateOfBirth, LocalDate.now()).years

        val weightUnit = if (user.isImperial) "lbs" else "kg"
        val heightUnit = if (user.isImperial) "ft" else "cm"
        val currencySymbol = user.currency.symbol

        val avoidedFoodsText = if (user.dislikedFoods.isNotEmpty()) {
            val foodsList = user.dislikedFoods.joinToString(", ") { it.name.replace("_", " ").lowercase() }
            "- Explicitly Avoid These Foods: $foodsList"
        } else {
            "- Explicitly Avoid These Foods: None"
        }

        // AI CALCULAT ASTA:
        val unitInstructions = if (user.isImperial) {
            """
            - SYSTEM: IMPERIAL (US Standard)
            - Weight: Use lbs and oz.
            - Volume: Use cups, tbsp, tsp, fl oz.
            - Do NOT use grams or ml.
            - Example: "4 oz chicken breast", "1/2 cup rice".
            """.trimIndent()
        } else {
            """
            - SYSTEM: METRIC (International)
            - Weight: Use grams (g) and kilograms (kg).
            - Volume: Use milliliters (ml) and liters (L).
            - Do NOT use cups, oz, or lbs.
            - Example: "150g chicken breast", "200ml milk".
            """.trimIndent()
        }

        val dietText = if (user.dietaryPreferences.isNotEmpty()) {
            user.dietaryPreferences.joinToString(", ") { it.name.lowercase().replace("_", " ") }
        } else {
            "Standard"
        }

        val medicalText = if (user.medicalConditions.isNotEmpty()) {
            user.medicalConditions.joinToString(", ") { it.name.lowercase().replace("_", " ") }
        } else {
            "None"
        }

        return """
        You are a backend server that outputs ONLY valid JSON.
        Do NOT use markdown, do NOT use ```json tags, do NOT add explanations.
        
        PROFILE:
        Age: $age, Gender: ${user.gender}, Weight: ${user.weight} $weightUnit, Goal: ${user.targetWeight} $weightUnit
        Diet: $dietText, Avoid: $avoidedFoodsText, Medical: $medicalText
        Calories: ${user.targetCalories} kcal/day
        $unitInstructions
        
        INSTRUCTIONS:
        1. Generate a JSON object containing a single key called "days".
        2. The "days" array MUST contain EXACTLY 14 objects.
        3. You MUST generate the objects for Day 1, Day 2, Day 3, Day 4, Day 5, Day 6, Day 7, Day 8, Day 9, Day 10, Day 11, Day 12, Day 13, and Day 14.
        4. Each day object MUST contain exactly these 5 keys: "dayNumber", "breakfast", "lunch", "dinner", and "snack".
        5. The meal keys ("breakfast", "lunch", "dinner", "snack") MUST be single JSON objects, NEVER arrays.
        6. The sum of calories for the 4 meals in a day MUST equal ${user.targetCalories}.
        7. You MUST use the exact same calorie split for all 14 days.
        
        FORMAT YOU MUST FOLLOW:
        {
          "days": [
            {
              "dayNumber": 1,
              "breakfast": {"name": "", "calories": 0, "protein": 0, "fat": 0, "carbs": 0, "quantityDetails": ""},
              "lunch": {"name": "", "calories": 0, "protein": 0, "fat": 0, "carbs": 0, "quantityDetails": ""},
              "dinner": {"name": "", "calories": 0, "protein": 0, "fat": 0, "carbs": 0, "quantityDetails": ""},
              "snack": {"name": "", "calories": 0, "protein": 0, "fat": 0, "carbs": 0, "quantityDetails": ""}
            }
          ]
        }
        Now, generate the full JSON array containing all 14 days based on the format above. Do not skip any days.
        """.trimIndent()
    }

    fun buildShoppingListPrompt(ingredients: List<String>, user: UserEntity): String {
        val ingredientsText = ingredients.joinToString("\n")

        val conversionInstructions = if (user.isImperial) {
            "Convert ALL quantities to IMPERIAL units (oz, lbs, cups). Even if input is in grams, convert to Imperial."
        } else {
            "Convert ALL quantities to METRIC units (g, kg, ml, L). Even if input is in cups/oz, convert to Metric."
        }

        return """
            IMPORTANT: Return ONLY raw JSON. No Markdown.
            Act as a smart shopping assistant. Aggregate these ingredients into a shopping list.
            
            TASKS:
            1. Sum up quantities (e.g., "2 eggs" + "3 eggs" = "5 eggs").
            2. Merge similar items (e.g., "Rolled oats" + "Oats" = "Oats").
            3. Group into categories: Produce, Meat, Dairy, Pantry, Frozen, Bakery, Spices, Beverages.
            4. UNIT CONVERSION: $conversionInstructions
            
            INPUT INGREDIENTS:
            $ingredientsText
            
            REQUIRED JSON STRUCTURE:
            {
              "categories": [
                {
                  "name": "Category Name",
                  "items": ["Item 1 with quantity", "Item 2 with quantity"]
                }
              ]
            }
        """.trimIndent()
    }

    private fun <T> callGemini(prompt: String, responseType: Class<T>): T? {
        val requestBody = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(requestBody, headers)
        val urlWithKey = "$apiUrl?key=$apiKey"

        return try {
            val response = restTemplate.postForObject(urlWithKey, entity, GeminiResponse::class.java)
            val rawJson = response?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw RuntimeException("Gemini response is null")

            val cleanJson = cleanJsonString(rawJson)
            objectMapper.readValue(cleanJson, responseType)
        } catch (e: Exception) {
            println("Gemini Error: ${e.message}")
            null
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
}