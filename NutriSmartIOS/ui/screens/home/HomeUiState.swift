//
//  HomeUiState.swift
//  NutriSmartIOS
//

import Foundation

struct HomeUiState {
    var isLoading: Bool = false
    var weight: Double = 0.0
    var steps: Int = 0
    var burnedCalories: Int = 0
    var isImperial: Bool = false
    
    var breakfast: MealDto? = nil
    var lunch: MealDto? = nil
    var dinner: MealDto? = nil
    var snack: MealDto? = nil
    
    var waterConsumedMl: Int = 0
    var waterGoalMl: Int = 2000
    var glassSizeMl: Int = 250
    
    var shoppingList: ShoppingListDto? = nil
    var alternatives: [MealDto] = []
    var isSwapping: Bool = false
    
    var wakeUpTime: Date = Calendar.current.date(bySettingHour: 8, minute: 0, second: 0, of: Date()) ?? Date()
    
    var bonusSnack: MealDto? = nil
    var availableBonusSnacks: [MealDto] = []
}
