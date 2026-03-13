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

enum class Currency(val symbol: String) {
    RON("RON"),
    EUR("€"),
    USD("$"),
    GBP("£")
}

enum class AuthProvider {
    LOCAL, GOOGLE
}