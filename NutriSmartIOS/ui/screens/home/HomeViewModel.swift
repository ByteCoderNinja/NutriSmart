//
//  HomeViewModel.swift
//  NutriSmartIOS
//

import Foundation
import SwiftUI
import Combine

@Observable
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
        
        userRepository.getUser(userId: userId) { [weak self] result in
            DispatchQueue.main.async {
                if case .success(let user) = result {
                    self?.uiState.weight = user.weight ?? 0.0
                    self?.uiState.isImperial = user.isImperial
                }
            }
        }
        
        mealRepository.getTodayPlan(userId: userId) { [weak self] result in
            DispatchQueue.main.async {
                self?.uiState.isLoading = false
                if case .success(let plan) = result {
                    self?.uiState.breakfast = plan.breakfast
                    self?.uiState.lunch = plan.lunch
                    self?.uiState.dinner = plan.dinner
                    self?.uiState.snack = plan.snack
                }
            }
        }
        
        NutriSmartService.shared.getShoppingList(userId: userId) { [weak self] result in
            DispatchQueue.main.async {
                if case .success(let list) = result {
                    self?.uiState.shoppingList = list
                }
            }
        }
    }
    
    func addWater() {
        uiState.waterConsumedMl += uiState.glassSizeMl
        waterRepository.saveWaterIntake(uiState.waterConsumedMl)
    }
    
    func removeWater() {
        uiState.waterConsumedMl = max(0, uiState.waterConsumedMl - uiState.glassSizeMl)
        waterRepository.saveWaterIntake(uiState.waterConsumedMl)
    }
    
    func toggleMeal(mealId: Int, type: MealType, consumed: Bool) {
        switch type {
        case .BREAKFAST: uiState.breakfast = uiState.breakfast.map { var m = $0; m.consumed = consumed; return m }
        case .LUNCH: uiState.lunch = uiState.lunch.map { var m = $0; m.consumed = consumed; return m }
        case .DINNER: uiState.dinner = uiState.dinner.map { var m = $0; m.consumed = consumed; return m }
        case .SNACK: uiState.snack = uiState.snack.map { var m = $0; m.consumed = consumed; return m }
        }
        
        mealRepository.toggleMealConsumed(mealId: mealId, consumed: consumed) { _ in }
    }
    
    func adjustWeight(delta: Double) {
        uiState.weight = max(30.0, uiState.weight + delta)
        let roundedWeight = (uiState.weight * 10).rounded() / 10
        uiState.weight = roundedWeight
        
        updateWeightTask?.cancel()
        updateWeightTask = Task {
            try? await Task.sleep(nanoseconds: 1_500 * 1_000_000)
            if Task.isCancelled { return }
            
            let request = UpdateUserRequest(
                username: nil, weight: roundedWeight, height: nil, targetWeight: nil,
                activityLevel: nil, maxDailyBudget: nil, dietaryPreferences: nil,
                medicalConditions: nil, dislikedFoods: nil, stepGoal: nil, isImperial: nil, currency: nil
            )
            userRepository.patchUser(userId: currentUserId, request: request) { _ in }
        }
    }
}
