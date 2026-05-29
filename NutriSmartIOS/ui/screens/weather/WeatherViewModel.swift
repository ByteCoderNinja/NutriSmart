//
//  WeatherViewModel.swift
//  NutriSmartIOS
//

import Foundation
import SwiftUI
import CoreLocation

struct WeatherUiState {
    var isLoading = true
    var temperature: Int = 0
    var condition: String = ""
    var humidity: Int = 0
    var cityName: String = "Locating..."
    var recommendations: [String] = []
}

@Observable
@MainActor
class WeatherViewModel {
    var uiState = WeatherUiState()
    private let weatherRepository = WeatherRepository.shared
    
    func fetchRealWeather(lat: Double, lon: Double) {
        uiState.isLoading = true
        Task {
            do {
                let response = try await weatherRepository.getCurrentWeather(lat: lat, lon: lon)
                let currentTemp = Int(response.main.temp)
                let currentCondition = response.weather.first?.main ?? "Unknown"
                let recs = generateRecommendations(temp: currentTemp, condition: currentCondition)
                
                uiState.temperature = currentTemp
                uiState.condition = currentCondition
                uiState.humidity = response.main.humidity
                uiState.cityName = response.name
                uiState.recommendations = recs
                uiState.isLoading = false
            } catch {
                uiState.condition = "Error"
                uiState.cityName = "Error"
                uiState.isLoading = false
            }
        }
    }
    
    private func generateRecommendations(temp: Int, condition: String) -> [String] {
        var recs: [String] = []
        if temp >= 30 {
            recs.append("It's a heatwave! Increase water intake.")
            recs.append("Avoid direct sun 11AM-4PM.")
        }
        if condition.lowercased().contains("rain") {
            recs.append("It's raining. Indoor workout?")
        } else if temp >= 15 && temp <= 29 {
            recs.append("Perfect weather for a walk!")
        } else {
            recs.append("Go out for a walk, but dress well.")
        }
        return recs
    }
}
