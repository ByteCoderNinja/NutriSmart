//
//  WaterRepository.swift
//  NutriSmartIOS
//

import Foundation

class WaterRepository {
    static let shared = WaterRepository()
    
    private let sessionManager = SessionManager.shared
    
    private init() {}
    
    func getWaterIntake() -> Int { sessionManager.getWaterIntake() }
    
    func saveWaterIntake(water: Int) {
        sessionManager.saveWaterIntake(ml: water)
    }
}
