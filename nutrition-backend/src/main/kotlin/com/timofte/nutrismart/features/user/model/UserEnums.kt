package com.timofte.nutrismart.features.user.model

enum class Gender {
    MALE, FEMALE
}

enum class ActivityLevel(val multiplier: Double) {
    SEDENTARY(1.2),
    LIGHTLY_ACTIVE(1.375),
    MODERATELY_ACTIVE(1.55),
    VERY_ACTIVE(1.75),
    EXTRA_ACTIVE(1.9)
}

enum class DietaryPreference {
    STANDARD,
    VEGAN,
    VEGETARIAN,
    KETO,
    HIGH_PROTEIN
}

enum class MedicalCondition {
    NONE,
    DIABETES_TYPE_1,
    DIABETES_TYPE_2,
    HYPERTENSION,
    LACTOSE_INTOLERANCE,
    GLUTEN_INTOLERANCE,
    HIGH_CHOLESTEROL,
    GASTRITIS
}

enum class Currency(val symbol: String) {
    RON("RON"),
    EUR("€"),
    USD("$"),
    GBP("£")
}