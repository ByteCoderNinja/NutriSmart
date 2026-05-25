//
//  MealRepository.swift
//  NutriSmartIOS
//

import Foundation

import Foundation

class MealRepository {
    static let shared = MealRepository()
    
    // Conectăm repository-ul la serviciul nostru de rețea
    private let api = NutriSmartApiService.shared
    
    private init() {}
    
    func getDailyPlan(token: String, userId: Int, date: String) async throws -> MealPlanDto {
        return try await api.getDailyPlan(token: token, userId: userId, date: date)
    }

    func getShoppingList(token: String, userId: Int) async throws -> ShoppingListDto {
        return try await api.getShoppingList(token: token, userId: userId)
    }

    func getMealAlternatives(token: String, userId: Int, type: String) async throws -> [MealDto] {
        return try await api.getMealAlternatives(token: token, userId: userId, mealType: type)
    }

    func swapMeal(token: String, userId: Int, type: String, mealId: Int, date: String) async throws -> MealPlanDto {
        return try await api.swapMeal(token: token, userId: userId, mealType: type, newMealId: mealId, date: date)
    }

    func toggleMealConsumed(token: String, mealId: Int, consumed: Bool) async throws -> MealDto {
        return try await api.toggleMealConsumed(token: token, mealId: mealId, consumed: consumed)
    }

    func toggleShoppingItem(token: String, itemId: Int, checked: Bool) async throws -> EmptyResponse {
        return try await api.toggleShoppingItem(token: token, itemId: itemId, isChecked: checked)
    }
}
