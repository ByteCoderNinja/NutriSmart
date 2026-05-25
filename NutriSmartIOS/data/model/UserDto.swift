//
//  UserDto.swift
//  NutriSmartIOS
//

import Foundation

struct UserDto: Codable {
    let id: Int
    let username: String
    let email: String
    let weight: Double?
    let height: Double?
    let dateOfBirth: String?
    let targetWeight: Double?
    let gender: Gender?
    let activityLevel: ActivityLevel?
    let maxDailyBudget: Double?
    let dietaryPreferences: [DietaryPreference]?
    let medicalConditions: [MedicalCondition]?
    let dislikedFoods: [String]?
    let stepGoal: Int?
    let isGoogleUser: Bool
    let isImperial: Bool
    let currency: Currency?
}

struct UpdateUserRequest: Codable {
    let username: String?
    let weight: Double?
    let height: Double?
    let targetWeight: Double?
    let activityLevel: ActivityLevel?
    let maxDailyBudget: Double?
    let dietaryPreferences: [DietaryPreference]?
    let medicalConditions: [MedicalCondition]?
    let dislikedFoods: [String]?
    let stepGoal: Int?
    let isImperial: Bool?
    let currency: Currency?
}
