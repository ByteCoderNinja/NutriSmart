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

enum class DislikedFood(val displayName: String) {
    BEEF("Beef"),
    PORK("Pork"),
    CHICKEN("Chicken"),
    LAMB("Lamb"),
    TURKEY("Turkey"),
    DUCK("Duck"),
    RABBIT("Rabbit"),
    VENISON("Venison"),
    ORGAN_MEATS("Organ Meats"),

    FISH("Fish"),
    SALMON("Salmon"),
    TUNA("Tuna"),
    WHITE_FISH("White Fish"),
    SEAFOOD("Seafood"),
    SHRIMP("Shrimp"),
    SQUID("Squid"),
    MUSSELS("Mussels"),

    EGGS("Eggs"),
    MILK("Milk"),
    YOGURT("Yogurt"),
    BUTTER("Butter"),
    CHEESE("Cheese"),
    COTTAGE_CHEESE("Cottage Cheese"),
    HARD_CHEESE("Hard Cheese"),
    CREAM("Cream"),

    SPINACH("Spinach"),
    BROCCOLI("Broccoli"),
    CAULIFLOWER("Cauliflower"),
    CABBAGE("Cabbage"),
    MUSHROOMS("Mushrooms"),
    ONIONS("Onions"),
    GARLIC("Garlic"),
    TOMATOES("Tomatoes"),
    EGGPLANT("Eggplant"),
    ZUCCHINI("Zucchini"),
    BELL_PEPPERS("Bell Peppers"),
    OLIVES("Olives"),
    CARROTS("Carrots"),
    CELERY("Celery"),
    ASPARAGUS("Asparagus"),
    PEAS("Peas"),
    CORN("Corn"),
    SWEET_POTATOES("Sweet Potatoes"),
    NETTLES("Nettles"),

    APPLES("Apples"),
    BANANAS("Bananas"),
    CITRUS_FRUITS("Citrus Fruits"),
    BERRIES("Berries"),
    MELONS("Melons"),
    AVOCADO("Avocado"),
    COCONUT("Coconut"),
    RAISINS("Raisins"),

    BEANS("Beans"),
    LENTILS("Lentils"),
    CHICKPEAS("Chickpeas"),
    NUTS("Nuts"),
    PEANUTS("Peanuts"),
    ALMONDS("Almonds"),
    WALNUTS("Walnuts"),
    SOY("Soy"),
    TOFU("Tofu"),

    CILANTRO("Cilantro"),
    DILL("Dill"),
    PARSLEY("Parsley"),
    MINT("Mint"),
    SPICY_FOOD("Spicy Food"),
    MUSTARD("Mustard"),
    MAYONNAISE("Mayonnaise")
}