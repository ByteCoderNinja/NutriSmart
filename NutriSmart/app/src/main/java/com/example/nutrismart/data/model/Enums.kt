package com.example.nutrismart.data.model

enum class Gender {
    MALE, FEMALE
}

enum class ActivityLevel(val label: String) {
    SEDENTARY("Sedentary (Little to no exercise)"),
    LIGHTLY_ACTIVE("Lightly Active (1-3 days/week)"),
    MODERATELY_ACTIVE("Moderately Active (3-5 days/week)"),
    VERY_ACTIVE("Very Active (6-7 days/week)"),
    EXTRA_ACTIVE("Extra Active (Physical job)")
}

enum class DietaryPreference {
    STANDARD,
    VEGAN,
    VEGETARIAN,
    PESCO_VEGETARIAN,
    KETO,
    PALEO,
    HIGH_PROTEIN
}

enum class MedicalCondition {
    NONE,
    DIABETES_TYPE_1,
    DIABETES_TYPE_2,
    HYPERTENSION,
    LACTOSE_INTOLERANCE,
    GLUTEN_INTOLERANCE,
    CELIAC_DISEASE,
    HIGH_CHOLESTEROL,
    GASTRITIS,
    IRRITABLE_BOWEL_SYNDROME,
    POLYCYSTIC_OVARY_SYNDROME,
    GOUT,
    HYPOTHYROIDISM
}

enum class Currency {
    RON, EUR, USD, GBP
}