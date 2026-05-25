//
//  MealModels.swift
//  NutriSmartIOS
//

import Foundation

struct MealDto: Codable, Identifiable {
    let id: Int
    let name: String
    let calories: Int
    let protein: Int
    let fat: Int
    let carbs: Int
    let quantityDetails: String?
    let consumed: Bool
}

struct MealPlanDto: Codable {
    let id: Int
    let breakfast: MealDto?
    let lunch: MealDto?
    let dinner: MealDto?
    let snack: MealDto?
}

struct ShoppingListDto: Codable {
    let id: Int
    let userId: Int
    let items: [ShoppingListItemDto]
}

struct ShoppingListItemDto: Codable, Identifiable {
    let id: Int
    let category: String
    let name: String
    let checked: Bool
}

struct EmptyResponse: Codable {}
