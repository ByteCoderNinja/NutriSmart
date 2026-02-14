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

    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

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

        return """
            IMPORTANT: Return ONLY the raw JSON. Do not include any Markdown formatting like \``json. Do not add any introductory text.
            Act as a professional nutritionist. Generate a 14-DAY meal plan (Day 1 to Day 14) for a user with the following profile:
            - Age: $age
            - Gender: ${user.gender}
            - Weight: ${user.weight} $weightUnit
            - Height: ${user.height} $heightUnit
            - Activity Level: ${user.activityLevel}
            - Goal: ${user.targetWeight} $weightUnit
            - Medical Conditions: ${user.medicalConditions}
            - Budget Constraint: ${user.maxDailyBudget} $currencySymbol / day
            - Calories: ${user.targetCalories} kcal/day
            
            IMPORTANT RULES FOR THE PLAN:
            1. Output Format: Strictly valid JSON.
            2. Duration: Generate exactly 14 days (Day 1 to Day 14).
            3. Smart Cooking Strategy (CRITICAL): Use a "Cook Once, Eat Twice" strategy where possible to save time and money. 
               - Example: Roast Chicken Dinner on Day 1 becomes Chicken Salad Lunch on Day 2.
               - Example: Make a large batch of Chili for Dinner Day 3 and eat leftovers for Lunch Day 4.
            4. Ingredients: Reuse perishable ingredients (like spinach, opened greek yogurt) across multiple days to minimize food waste.
            5. Variety: Ensure meals are not repetitive (don't eat the same breakfast 14 days in a row), but keep the shopping list practical.
            
            $unitInstructions
            
            JSON STRUCTURE MUST BE EXACTLY THIS:
            {
              "days": [
                {
                  "dayNumber": 1,
                  "breakfast": { "name": "...", "calories": 400, "protein": 20, "fat": 15, "carbs": 45, "quantityDetails": "..." },
                  "lunch": { "name": "...", "calories": 600, "protein": 35, "fat": 20, "carbs": 65, "quantityDetails": "..." },
                  "dinner": { "name": "...", "calories": 500, "protein": 30, "fat": 15, "carbs": 55, "quantityDetails": "..." },
                  "snack": { "name": "...", "calories": 200, "protein": 10, "fat": 10, "carbs": 20, "quantityDetails": "..." }
                },
                ... (repeat for days 1 to 14)
              ]
            }
            "quantityDetails" should be descriptive (e.g., "2 eggs, 1 slice toast").
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