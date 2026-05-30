//
//  HomeViewModel.swift
//  NutriSmartIOS
//

import Foundation
import SwiftUI
import Combine

@Observable
@MainActor
class HomeViewModel {
    var uiState = HomeUiState()
    
    private let waterRepository = WaterRepository.shared
    private let userRepository = UserRepository.shared
    private let mealRepository = MealRepository.shared
    private let authRepository = AuthRepository.shared
    
    private let currentUserId: Int
    private var updateWeightTask: Task<Void, Never>? = nil
    
    init() {
        self.currentUserId = SessionManager.shared.fetchUserId()
        
        let savedWater = waterRepository.getWaterIntake()
        let wakeUpTimeStr = authRepository.getWakeUpTime()
        
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        let wakeUpTime = formatter.date(from: wakeUpTimeStr) ?? Calendar.current.date(bySettingHour: 8, minute: 0, second: 0, of: Date())!
        
        uiState.waterConsumedMl = savedWater
        uiState.wakeUpTime = wakeUpTime
        
        if currentUserId != -1 {
            fetchTodayData()
        }
    }
    
    func fetchTodayData() {
        uiState.isLoading = true
        
        let userId = currentUserId
        let token = SessionManager.shared.fetchAuthToken() ?? ""
        
        Task {
            // HealthKit
            if await HealthKitManager.shared.requestAuthorization() {
                let steps = await HealthKitManager.shared.getTodaySteps()
                uiState.steps = steps
                // Assume 0.04 kcal per step for simple demo
                uiState.burnedCalories = Int(Double(steps) * 0.04)
            }
            
            do {
                let user = try await userRepository.getUser(token: token, userId: userId)
                uiState.weight = user.weight ?? 0.0
                uiState.isImperial = user.isImperial
            } catch {
                print("Error fetching user: \(error)")
            }
            
            do {
                let plan = try await mealRepository.getTodayPlan(token: token, userId: userId)
                uiState.breakfast = plan.breakfast
                uiState.lunch = plan.lunch
                uiState.dinner = plan.dinner
                uiState.snack = plan.snack
            } catch {
                print("Error fetching meal plan: \(error)")
            }
            
            do {
                let list = try await mealRepository.getShoppingList(token: token, userId: userId)
                uiState.shoppingList = list
            } catch {
                print("Error fetching shopping list: \(error)")
            }
            
            uiState.isLoading = false
        }
    }
    
    func loadAlternatives(type: String) {
        uiState.isSwapping = true
        uiState.alternatives = []
        
        let userId = currentUserId
        let token = SessionManager.shared.fetchAuthToken() ?? ""
        
        Task {
            do {
                let list = try await mealRepository.getMealAlternatives(token: token, userId: userId, type: type.uppercased())
                uiState.alternatives = list
                uiState.isSwapping = false
            } catch {
                print("Error loading alternatives: \(error)")
                uiState.isSwapping = false
            }
        }
    }
    
    func swapMeal(type: String, newMealId: Int) {
        let userId = currentUserId
        let token = SessionManager.shared.fetchAuthToken() ?? ""
        let date = {
            let f = DateFormatter()
            f.dateFormat = "yyyy-MM-dd"
            return f.string(from: Date())
        }()
        
        Task {
            do {
                _ = try await mealRepository.swapMeal(token: token, userId: userId, type: type.uppercased(), mealId: newMealId, date: date)
                fetchTodayData()
            } catch {
                print("Error swapping meal: \(error)")
            }
        }
    }
    
    func loadBonusSnacks() {
        uiState.isSwapping = true
        uiState.availableBonusSnacks = []
        
        let userId = currentUserId
        let token = SessionManager.shared.fetchAuthToken() ?? ""
        
        Task {
            do {
                let allSnacks = try await mealRepository.getMealAlternatives(token: token, userId: userId, type: "SNACK")
                let maxCalories = uiState.burnedCalories
                uiState.availableBonusSnacks = allSnacks.filter { $0.calories <= maxCalories }
                uiState.isSwapping = false
            } catch {
                print("Error loading bonus snacks: \(error)")
                uiState.isSwapping = false
            }
        }
    }
    
    func updateWaterGoalBasedOnWeather(temp: Int) {
        let newGoal = temp >= 30 ? 3000 : 2000
        uiState.waterGoalMl = newGoal
    }
    
    func addWater() {
        uiState.waterConsumedMl += uiState.glassSizeMl
        waterRepository.saveWaterIntake(water: uiState.waterConsumedMl)
    }
    
    func removeWater() {
        uiState.waterConsumedMl = max(0, uiState.waterConsumedMl - uiState.glassSizeMl)
        waterRepository.saveWaterIntake(water: uiState.waterConsumedMl)
    }
    
    func toggleMeal(mealId: Int, type: MealType, consumed: Bool) {
        switch type {
        case .BREAKFAST: uiState.breakfast = uiState.breakfast.map { var m = $0; m.consumed = consumed; return m }
        case .LUNCH: uiState.lunch = uiState.lunch.map { var m = $0; m.consumed = consumed; return m }
        case .DINNER: uiState.dinner = uiState.dinner.map { var m = $0; m.consumed = consumed; return m }
        case .SNACK: uiState.snack = uiState.snack.map { var m = $0; m.consumed = consumed; return m }
        }
        
        let token = SessionManager.shared.fetchAuthToken() ?? ""
        Task {
            try? await mealRepository.toggleMealConsumed(token: token, mealId: mealId, consumed: consumed)
        }
    }
    
    func toggleShoppingListItem(itemId: Int, checked: Bool) {
        if var list = uiState.shoppingList {
            if let index = list.items.firstIndex(where: { $0.id == itemId }) {
                let updatedItem = ShoppingListItemDto(
                    id: list.items[index].id,
                    category: list.items[index].category,
                    name: list.items[index].name,
                    checked: checked
                )
                var newItems = list.items
                newItems[index] = updatedItem
                uiState.shoppingList = ShoppingListDto(id: list.id, userId: list.userId, items: newItems)
            }
        }
        
        let token = SessionManager.shared.fetchAuthToken() ?? ""
        Task {
            try? await mealRepository.toggleShoppingItem(token: token, itemId: itemId, checked: checked)
        }
    }
    
    func adjustWeight(delta: Double) {
        uiState.weight = max(30.0, uiState.weight + delta)
        let roundedWeight = (uiState.weight * 10).rounded() / 10
        uiState.weight = roundedWeight
        
        updateWeightTask?.cancel()
        updateWeightTask = Task {
            try? await Task.sleep(nanoseconds: 1_500 * 1_000_000)
            if Task.isCancelled { return }
            
            let token = SessionManager.shared.fetchAuthToken() ?? ""
            let request = UpdateUserRequest(
                username: nil, weight: roundedWeight, height: nil, targetWeight: nil,
                activityLevel: nil, maxDailyBudget: nil, dietaryPreferences: nil,
                medicalConditions: nil, dislikedFoods: nil, stepGoal: nil, isImperial: nil, currency: nil
            )
            try? await userRepository.patchUser(token: token, userId: currentUserId, request: request)
        }
    }
}
