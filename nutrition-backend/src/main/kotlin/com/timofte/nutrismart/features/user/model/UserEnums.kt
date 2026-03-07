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

enum class DislikedFood {
    BEEF,
    PORK,
    CHICKEN,
    LAMB,
    TURKEY,
    DUCK,
    RABBIT,
    VENISON,
    ORGAN_MEATS,

    FISH,
    SALMON,
    TUNA,
    WHITE_FISH,
    SEAFOOD,
    SHRIMP,
    SQUID,
    MUSSELS,

    EGGS,
    MILK,
    YOGURT,
    BUTTER,
    CHEESE,
    COTTAGE_CHEESE,
    HARD_CHEESE,
    CREAM,

    SPINACH,
    BROCCOLI,
    CAULIFLOWER,
    CABBAGE,
    MUSHROOMS,
    ONIONS,
    GARLIC,
    TOMATOES,
    EGGPLANT,
    ZUCCHINI,
    BELL_PEPPERS,
    OLIVES,
    CARROTS,
    CELERY,
    ASPARAGUS,
    PEAS,
    CORN,
    SWEET_POTATOES,
    NETTLES,

    APPLES,
    BANANAS,
    CITRUS_FRUITS,
    BERRIES,
    MELONS,
    AVOCADO,
    COCONUT,
    RAISINS,

    BEANS,
    LENTILS,
    CHICKPEAS,
    NUTS,
    PEANUTS,
    ALMONDS,
    WALNUTS,
    SOY,
    TOFU,

    CILANTRO,
    DILL,
    PARSLEY,
    MINT,
    SPICY_FOOD,
    MUSTARD,
    MAYONNAISE
}