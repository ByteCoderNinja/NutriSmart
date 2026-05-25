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
class WeatherViewModel {
    var uiState = WeatherUiState()
    private let weatherRepository = WeatherRepository.shared
    func fetchRealWeather(lat: Double, lon: Double) {
        uiState.isLoading = true
        weatherRepository.getCurrentWeather(lat: lat, lon: lon) { [weak self] result in
            DispatchQueue.main.async {
                self?.uiState.isLoading = false
                switch result {
                case .success(let response):
                    let currentTemp = Int(response.main.temp)
                    let currentCondition = response.weather.first?.main ?? "Unknown"
                    let recs = self?.generateRecommendations(temp: currentTemp, condition: currentCondition) ?? []
                    self?.uiState.temperature = currentTemp; self?.uiState.condition = currentCondition; self?.uiState.humidity = response.main.humidity; self?.uiState.cityName = response.name; self?.uiState.recommendations = recs
                case .failure(_): self?.uiState.condition = "Error"; self?.uiState.cityName = "Error"
                }
            }
        }
    }
    private func generateRecommendations(temp: Int, condition: String) -> [String] {
        var recs: [String] = []
        if temp >= 30 { recs.append("It's a heatwave! Increase water intake."); recs.append("Avoid direct sun 11AM-4PM.") }
        if condition.lowercased().contains("rain") { recs.append("It's raining. Indoor workout?") }
        else if temp >= 15 && temp <= 29 { recs.append("Perfect weather for a walk!") }
        else { recs.append("Go out for a walk, but dress well.") }
        return recs
    }
}
