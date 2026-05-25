//
//  FastingRepository.swift
//  NutriSmartIOS
//

import Foundation

class FastingRepository {
    static let shared = FastingRepository()
    private let sessionManager = SessionManager.shared
    
    private init() {}
    
    func isFastingActive() -> Bool { sessionManager.isFastingActive() }
    func getFastingEndTime() -> Date { sessionManager.getFastingEndTime() }
    func getFastingStartTime() -> Date { sessionManager.getFastingStartTime() }
    func getFastingDurationHours() -> Int { sessionManager.getFastingDurationHours() }
    func clearFastingState() { sessionManager.clearFastingState() }
    
    func saveFastingState(isActive: Bool, startTime: Date, endTime: Date, durationHours: Int) {
        sessionManager.saveFastingState(isActive: isActive, startTime: startTime, endTime: endTime, durationHours: durationHours)
    }
}
