//
//  OnboardingViewModel.swift
//  NutriSmartIOS
//

import Foundation
import SwiftUI
import Combine

@Observable
class OnboardingViewModel {
    var birthDate = Calendar.current.date(byAdding: .year, value: -18, to: Date()) ?? Date()
    var gender: Gender = .MALE
    var height = ""
    var weight = ""
    var targetWeight = ""
    var activityLevel: ActivityLevel = .SEDENTARY
    var budget = ""
    var isImperial = false
    var currency: Currency = .RON
    var selectedDietaryPreferences: [DietaryPreference] = []
    var selectedMedicalConditions: [MedicalCondition] = []
    var selectedDislikedFoods: Set<String> = []
    var foodSearchQuery = ""
    var isSearchingFoods = false
    var foodSuggestions: [String] = []
    var isLoading = false
    var errorMessage: String? = nil
    var isComplete = false
    var loadingMessage = "Saving profile..."
    
    private let userRepository = UserRepository.shared
    private let token: String
    private let userId: Int
    private var searchTask: Task<Void, Never>? = nil
    private var messageTask: Task<Void, Never>? = nil
    
    private let messages = [
        "Analyzing your profile...", "Calculating target calories...",
        "Generating 14-day AI recipes...", "Finalizing your plan...",
        "Do not panic if it looks stuck\nAI is working"
    ]
    
    init() {
        self.token = SessionManager.shared.fetchAuthToken() ?? ""
        self.userId = SessionManager.shared.fetchUserId()
    }
    
    func loadUserData() {
        guard userId != -1 else { return }
        isLoading = true
        loadingMessage = "Loading your details..."
        userRepository.getUser(userId: userId) { [weak self] result in
            DispatchQueue.main.async {
                self?.isLoading = false
                if case .success(let user) = result {
                    if let dobStr = user.dateOfBirth {
                        let formatter = ISO8601DateFormatter()
                        formatter.formatOptions = [.withFullDate, .withDashSeparatorInDate]
                        if let date = formatter.date(from: dobStr) { self?.birthDate = date }
                    }
                    if let gender = user.gender { self?.gender = gender }
                    if let h = user.height { self?.height = String(h) }
                    if let w = user.weight { self?.weight = String(w) }
                    if let tw = user.targetWeight { self?.targetWeight = String(tw) }
                    if let al = user.activityLevel { self?.activityLevel = al }
                    if let b = user.maxDailyBudget { self?.budget = String(b) }
                    if let c = user.currency { self?.currency = c }
                    self?.isImperial = user.isImperial
                    self?.selectedDietaryPreferences = user.dietaryPreferences ?? []
                    self?.selectedMedicalConditions = user.medicalConditions ?? []
                    self?.selectedDislikedFoods = Set(user.dislikedFoods ?? [])
                }
            }
        }
    }
    
    func toggleDiet(_ diet: DietaryPreference) {
        if let index = selectedDietaryPreferences.firstIndex(of: diet) { selectedDietaryPreferences.remove(at: index) }
        else { selectedDietaryPreferences.append(diet) }
    }
    func toggleCondition(_ condition: MedicalCondition) {
        if let index = selectedMedicalConditions.firstIndex(of: condition) { selectedMedicalConditions.remove(at: index) }
        else { selectedMedicalConditions.append(condition) }
    }
    func toggleDislikedFood(_ food: String) {
        if selectedDislikedFoods.contains(food) { selectedDislikedFoods.remove(food) }
        else { selectedDislikedFoods.insert(food) }
    }
    func onFoodSearchQueryChanged(_ newQuery: String) {
        foodSearchQuery = newQuery
        if newQuery.isEmpty { foodSuggestions = []; return }
        searchFoods(query: newQuery)
    }
    private func searchFoods(query: String) {
        searchTask?.cancel()
        searchTask = Task {
            try? await Task.sleep(nanoseconds: 400 * 1_000_000)
            if Task.isCancelled { return }
            await MainActor.run { isSearchingFoods = true }
            userRepository.searchFoods(query: query) { [weak self] result in
                DispatchQueue.main.async {
                    self?.isSearchingFoods = false
                    if case .success(let suggestions) = result { self?.foodSuggestions = suggestions }
                }
            }
        }
    }
    func submitProfile() {
        guard let hValue = Double(height.replacingOccurrences(of: ",", with: ".")),
              let wValue = Double(weight.replacingOccurrences(of: ",", with: ".")),
              let twValue = Double(targetWeight.replacingOccurrences(of: ",", with: ".")),
              let bValue = Double(budget.replacingOccurrences(of: ",", with: ".")) else {
            errorMessage = "Please fill in all numeric fields correctly."
            return
        }
        if birthDate > Date() { errorMessage = "Birth date cannot be in the future."; return }
        isLoading = true; errorMessage = nil; loadingMessage = "Saving profile..."
        let formatter = DateFormatter(); formatter.dateFormat = "yyyy-MM-dd"
        let request = OnboardingRequest(
            dateOfBirth: formatter.string(from: birthDate), gender: gender, height: hValue, weight: wValue,
            targetWeight: twValue, activityLevel: activityLevel, maxDailyBudget: bValue,
            dietaryPreferences: selectedDietaryPreferences, medicalConditions: selectedMedicalConditions,
            dislikedFoods: Array(selectedDislikedFoods), isImperial: isImperial, currency: currency
        )
        userRepository.completeProfile(request: request) { [weak self] result in
            DispatchQueue.main.async {
                switch result {
                case .success(_): self?.startPlanGeneration()
                case .failure(let error): self?.isLoading = false; self?.errorMessage = "Error saving profile: \(error)"
                }
            }
        }
    }
    private func startPlanGeneration() {
        isLoading = true; loadingMessage = messages[0]
        launchMessageRotation()
        pollForStatus()
    }
    private func launchMessageRotation() {
        messageTask?.cancel()
        messageTask = Task {
            var index = 0
            while !Task.isCancelled {
                await MainActor.run {
                    loadingMessage = messages[index]
                    if index < messages.count - 1 { index += 1 }
                }
                try? await Task.sleep(nanoseconds: 6 * 1_000_000_000)
            }
        }
    }
    private func pollForStatus() {
        Task {
            var isDone = false
            while !isDone && !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 5 * 1_000_000_000)
                ApiClient.shared.request(endpoint: "nutrition/status/\(userId)") { [weak self] (result: Result<[String: String], NetworkError>) in
                    DispatchQueue.main.async {
                        if case .success(let dict) = result {
                            let status = dict["status"] ?? "UNKNOWN"
                            if status == "COMPLETED" {
                                isDone = true; self?.messageTask?.cancel()
                                self?.loadingMessage = "Plan generated successfully!"; self?.isLoading = false; self?.isComplete = true
                            } else if status == "FAILED" {
                                isDone = true; self?.messageTask?.cancel()
                                self?.errorMessage = "AI generation failed. Please try again."; self?.isLoading = false
                            }
                        }
                    }
                }
            }
        }
    }
}
