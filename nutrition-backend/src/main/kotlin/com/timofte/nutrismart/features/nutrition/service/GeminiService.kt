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
        Return ONLY valid, raw JSON. Do not include any Markdown formatting like `json`. Do not add any introductory text.
        Act as a professional nutritionist. Generate a 14-DAY meal plan.
        
        USER PROFILE:
        - Age: $age
        - Gender: ${user.gender}
        - Weight: ${user.weight} $weightUnit
        - Height: ${user.height} $heightUnit
        - Activity Level: ${user.activityLevel}
        - Goal: ${user.targetWeight} $weightUnit
        - Diet: $dietText
        - Avoid: $avoidedFoodsText
        - Medical: $medicalText
        - Budget: ${user.maxDailyBudget} $currencySymbol / day
        - Target Calories: ${user.targetCalories} kcal/day
        
        NUTRITION RULES:
        1. STRICT DIET: Follow the $dietText diet completely.
        2. AVERSIONS: NEVER include any ingredients from the 'Avoid' list.
        3. CONSISTENT CALORIES: The daily target is ${user.targetCalories} kcal. Split this across 4 meals (Breakfast, Lunch, Dinner, Snack). You MUST use the EXACT SAME calorie split for ALL 14 days. (e.g., if Day 1 Breakfast is 400 kcal, Day 2 to Day 14 Breakfasts MUST also be exactly 400 kcal).
        
        CRITICAL JSON STRUCTURE RULES (NO EXCEPTIONS):
        1. The root MUST be a single object with the key "days".
        2. "days" MUST be an array of EXACTLY 14 day objects (Day 1 through Day 14).
        3. EVERY day object MUST have exactly these 5 keys: "dayNumber", "breakfast", "lunch", "dinner", and "snack".
        4. "breakfast", "lunch", "dinner", and "snack" MUST ALWAYS BE SINGLE OBJECTS. They CANNOT be arrays.
        5. You must write out the full independent object for Day 1, then Day 2, all the way to Day 14. Do not link days together.
        
        EXPECTED JSON TEMPLATE:
        {
          "days": [
            {
              "dayNumber": 1,
              "breakfast": {"name": "...", "calories": 400, "protein": 20, "fat": 15, "carbs": 45, "quantityDetails": "..."},
              "lunch": {"name": "...", "calories": 600, "protein": 35, "fat": 20, "carbs": 65, "quantityDetails": "..."},
              "dinner": {"name": "...", "calories": 500, "protein": 30, "fat": 15, "carbs": 55, "quantityDetails": "..."},
              "snack": {"name": "...", "calories": 200, "protein": 10, "fat": 10, "carbs": 20, "quantityDetails": "..."}
            },
            {
              "dayNumber": 2,
              "breakfast": {"name": "...", "calories": 400, "protein": 20, "fat": 15, "carbs": 45, "quantityDetails": "..."},
              "lunch": {"name": "...", "calories": 600, "protein": 35, "fat": 20, "carbs": 65, "quantityDetails": "..."},
              "dinner": {"name": "...", "calories": 500, "protein": 30, "fat": 15, "carbs": 55, "quantityDetails": "..."},
              "snack": {"name": "...", "calories": 200, "protein": 10, "fat": 10, "carbs": 20, "quantityDetails": "..."}
            }
            // CONTINUE THIS EXACT SAME STRUCTURE FOR dayNumber 3 THROUGH 14.
          ]
        }
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