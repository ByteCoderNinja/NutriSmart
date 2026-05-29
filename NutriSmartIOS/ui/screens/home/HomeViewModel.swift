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
