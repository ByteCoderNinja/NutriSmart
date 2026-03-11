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
        IMPORTANT: Return ONLY valid, raw JSON. Do not include any Markdown formatting like `json. Do not add any introductory text.
        Act as a professional nutritionist. Generate a 14-DAY meal plan (Day 1 to Day 14) for a user with the following profile:
        - Age: $age
        - Gender: ${user.gender}
        - Weight: ${user.weight} $weightUnit
        - Height: ${user.height} $heightUnit
        - Activity Level: ${user.activityLevel}
        - Goal: ${user.targetWeight} $weightUnit
        - Diet: $dietText
        - Medical Conditions: $medicalText
        $avoidedFoodsText
        - Budget Constraint: ${user.maxDailyBudget} $currencySymbol / day
        - Calories: ${user.targetCalories} kcal/day
        
        CRITICAL JSON STRUCTURE RULES (READ CAREFULLY):
        1. The root MUST be an object with exactly ONE key: "days".
        2. "days" MUST be an array containing EXACTLY 14 objects.
        3. EVERY single day object in the "days" array MUST have exactly 5 keys: "dayNumber", "breakfast", "lunch", "dinner", and "snack".
        4. "breakfast", "lunch", "dinner", and "snack" MUST be single objects, NOT arrays. Do NOT group meals together. Do NOT put 14 lunches inside one day.
        5. You MUST write out a full object for Day 1, Day 2, Day 3... all the way to Day 14.
        
        NUTRITION & PLANNING RULES:
        6. Smart Cooking Strategy: Use a "Cook Once, Eat Twice" strategy where possible. Example: Roast Chicken Dinner on Day 1 becomes Chicken Salad Lunch on Day 2.
        7. Variety & Ingredients: Reuse perishable ingredients across multiple days to minimize waste. Ensure meals are not repetitive, but keep the shopping list practical.
        8. AVERSIONS (CRITICAL): Do NOT include any ingredients listed in 'Explicitly Avoid These Foods'!
        9. DIETARY REQUIREMENT (STRICT): The user follows a $dietText diet.
           - IF VEGAN: NO animal products.
           - IF VEGETARIAN: NO meat, poultry, or fish.
           - IF PESCO-VEGETARIAN: NO meat or poultry (fish allowed).
           - IF KETO: High-fat, moderate-protein, extremely low-carb.
           - IF PALEO: NO grains, legumes, dairy, processed sugars.
        10. CONSISTENT CALORIES (CRITICAL):
           - Breakfast, Lunch, Dinner, and Snack must EACH have a fixed calorie target.
           - Example: If Breakfast is 400 kcal on Day 1, it MUST be exactly 400 kcal on Days 2 through 14.
           - The sum of (Breakfast + Lunch + Dinner + Snack) MUST exactly equal ${user.targetCalories} kcal EVERY SINGLE DAY.
        
        $unitInstructions
        
        EXPECTED JSON FORMAT:
        {
          "days": [
            {
              "dayNumber": 1,
              "breakfast": { "name": "Example 1", "calories": 400, "protein": 20, "fat": 15, "carbs": 45, "quantityDetails": "2 eggs" },
              "lunch": { "name": "Example 2", "calories": 600, "protein": 35, "fat": 20, "carbs": 65, "quantityDetails": "150g chicken" },
              "dinner": { "name": "Example 3", "calories": 500, "protein": 30, "fat": 15, "carbs": 55, "quantityDetails": "200g salmon" },
              "snack": { "name": "Example 4", "calories": 200, "protein": 10, "fat": 10, "carbs": 20, "quantityDetails": "1 apple" }
            },
            {
              "dayNumber": 2,
              "breakfast": { "name": "Example 5", "calories": 400, "protein": 20, "fat": 15, "carbs": 45, "quantityDetails": "Oatmeal" },
              "lunch": { "name": "Example 6", "calories": 600, "protein": 35, "fat": 20, "carbs": 65, "quantityDetails": "Leftover salmon" },
              "dinner": { "name": "Example 7", "calories": 500, "protein": 30, "fat": 15, "carbs": 55, "quantityDetails": "Beef steak" },
              "snack": { "name": "Example 8", "calories": 200, "protein": 10, "fat": 10, "carbs": 20, "quantityDetails": "Almonds" }
            }
          ]
        }
        Generate ALL 14 objects in the "days" array following the exact strict structure shown for Day 1 and Day 2.
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