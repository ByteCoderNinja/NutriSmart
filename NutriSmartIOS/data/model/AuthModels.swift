//
//  AuthModels.swift
//  NutriSmartIOS
//

import Foundation

struct AuthRequest: Codable {
    let email: String
    let password: String
}

struct RegisterRequest: Codable {
    let email: String
    let password: String
    let username: String
}

struct VerifyRequest: Codable {
    let email: String
    let code: String
}

struct AuthResponse: Codable {
    let token: String
    let userId: Int
    let isProfileComplete: Bool
    let isVerified: Bool
}

struct GoogleLoginRequest: Codable {
    let token: String
}

struct OnboardingRequest: Codable {
    let dateOfBirth: String
    let gender: Gender
    let height: Double
    let weight: Double
    let targetWeight: Double
    let activityLevel: ActivityLevel
    let maxDailyBudget: Double
    let dietaryPreferences: [DietaryPreference]
    let medicalConditions: [MedicalCondition]
    let dislikedFoods: [String]
    var isImperial: Bool = false
    var currency: Currency = .RON
}

struct WeatherResponse: Codable {
    let main: MainData
    let weather: [WeatherData]
    let name: String
}

struct MainData: Codable {
    let temp: Double
    let humidity: Int
}

struct WeatherData: Codable {
    let main: String
    let description: String
}
