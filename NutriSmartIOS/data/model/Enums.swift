//
//  Enums.swift
//  NutriSmartIOS
//

import Foundation

enum Gender: String, Codable, CaseIterable {
    case MALE, FEMALE
}

enum ActivityLevel: String, Codable, CaseIterable {
    case SEDENTARY = "SEDENTARY"
    case LIGHTLY_ACTIVE = "LIGHTLY_ACTIVE"
    case MODERATELY_ACTIVE = "MODERATELY_ACTIVE"
    case VERY_ACTIVE = "VERY_ACTIVE"
    case EXTRA_ACTIVE = "EXTRA_ACTIVE"
    
    var label: String {
        switch self {
        case .SEDENTARY: return "Sedentary (Little to no exercise)"
        case .LIGHTLY_ACTIVE: return "Lightly Active (1-3 days/week)"
        case .MODERATELY_ACTIVE: return "Moderately Active (3-5 days/week)"
        case .VERY_ACTIVE: return "Very Active (6-7 days/week)"
        case .EXTRA_ACTIVE: return "Extra Active (Physical job)"
        }
    }
}

enum DietaryPreference: String, Codable, CaseIterable {
    case STANDARD, VEGAN, VEGETARIAN, PESCO_VEGETARIAN, KETO, PALEO, HIGH_PROTEIN
}

enum MedicalCondition: String, Codable, CaseIterable {
    case NONE
    case DIABETES_TYPE_1, DIABETES_TYPE_2, HYPERTENSION
    case LACTOSE_INTOLERANCE, GLUTEN_INTOLERANCE, CELIAC_DISEASE
    case HIGH_CHOLESTEROL, GASTRITIS, IRRITABLE_BOWEL_SYNDROME
    case POLYCYSTIC_OVARY_SYNDROME, GOUT, HYPOTHYROIDISM
}

enum Currency: String, Codable, CaseIterable {
    case RON, EUR, USD, GBP
}

enum MealType: String, Codable, CaseIterable {
    case BREAKFAST, LUNCH, DINNER, SNACK
}
