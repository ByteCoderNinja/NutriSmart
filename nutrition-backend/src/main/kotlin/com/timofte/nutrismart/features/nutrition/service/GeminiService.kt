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
        IMPORTANT: Return ONLY the raw JSON. Do not include any Markdown formatting like `json. Do not add any introductory text.
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
        
        CRITICAL GENERATION RULES:
        1. MANDATORY LENGTH: The `days` array MUST contain exactly 14 objects. You must output Day 1, Day 2, Day 3, Day 4, Day 5, Day 6, Day 7, Day 8, Day 9, Day 10, Day 11, Day 12, Day 13, and Day 14. Do NOT skip any days. Do NOT abbreviate.
        2. MANDATORY MEALS PER DAY: EVERY single day object MUST contain exactly 4 meal objects: "breakfast", "lunch", "dinner", and "snack". Never skip a meal in any day.
        
        IMPORTANT RULES FOR THE PLAN:
        3. Smart Cooking Strategy: Use a "Cook Once, Eat Twice" strategy where possible to save time and money. 
           - Example: Roast Chicken Dinner on Day 1 becomes Chicken Salad Lunch on Day 2.
        4. Ingredients: Reuse perishable ingredients across multiple days to minimize food waste.
        5. Variety: Ensure meals are not repetitive, but keep the shopping list practical.
        6. AVERSIONS (CRITICAL): Do NOT include any ingredients listed in 'Explicitly Avoid These Foods' under any circumstances!
        7. DIETARY REQUIREMENT (STRICT ADHERENCE REQUIRED): The user follows a $dietText diet.
           - IF VEGAN: NO animal products (no meat, poultry, fish, seafood, dairy, eggs, honey).
           - IF VEGETARIAN: NO meat, poultry, or fish.
           - IF PESCO-VEGETARIAN: NO meat or poultry (fish/seafood allowed).
           - IF KETO: High-fat, moderate-protein, extremely low-carb.
           - IF PALEO: NO grains, NO legumes, NO dairy, NO processed sugars.
        8. CONSISTENT MEAL CALORIES ACROSS ALL DAYS (CRITICAL): 
           - Determine a fixed calorie target for each meal type so their sum equals exactly ${user.targetCalories} kcal.
           - You MUST use these EXACT SAME calorie numbers for EVERY SINGLE DAY (Days 1 through 14).
           - Example: If Day 1 Breakfast is 400 kcal, then Day 2 to Day 14 Breakfasts MUST ALSO be exactly 400 kcal. Do NOT vary calories for the same meal type.
        
        $unitInstructions
        
        JSON STRUCTURE MUST BE EXACTLY THIS:
        {
          "days": [
            {
              "dayNumber": 1,
              "breakfast": { "name": "...", "calories": 0, "protein": 0, "fat": 0, "carbs": 0, "quantityDetails": "..." },
              "lunch": { "name": "...", "calories": 0, "protein": 0, "fat": 0, "carbs": 0, "quantityDetails": "..." },
              "dinner": { "name": "...", "calories": 0, "protein": 0, "fat": 0, "carbs": 0, "quantityDetails": "..." },
              "snack": { "name": "...", "calories": 0, "protein": 0, "fat": 0, "carbs": 0, "quantityDetails": "..." }
            },
            // You MUST continue outputting objects for dayNumber 2 through 14 in this exact same format.
          ]
        }
        "quantityDetails" should be descriptive (e.g., "2 eggs, 1 slice toast").
        Before outputting the JSON, internally verify that you have generated exactly 14 day objects, each with 4 meals.
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