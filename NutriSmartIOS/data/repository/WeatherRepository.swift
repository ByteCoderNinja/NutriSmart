//
//  WeatherRepository.swift
//  NutriSmartIOS
//

import Foundation

class WeatherRepository {
    static let shared = WeatherRepository()
    private let weatherService = WeatherService.shared
    
    private init() {}
    
    func getCurrentWeather(lat: Double, lon: Double) async throws -> WeatherResponse {
        return try await weatherService.getCurrentWeather(lat: lat, lon: lon)
    }
}
