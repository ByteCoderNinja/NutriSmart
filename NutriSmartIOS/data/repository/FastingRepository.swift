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
    func getFastingEndTime() -> Date {
        let time = sessionManager.getFastingEndTime()
        return Date(timeIntervalSince1970: TimeInterval(time))
    }
    
    func getFastingStartTime() -> Date {
        let time = sessionManager.getFastingStartTime()
        return Date(timeIntervalSince1970: TimeInterval(time))
    }
    
    func getFastingDurationHours() -> Int { sessionManager.getFastingDurationHours() }
    func clearFastingState() { sessionManager.clearFastingState() }
    
    func saveFastingState(isActive: Bool, startTime: Date, endTime: Date, durationHours: Int) {
        sessionManager.saveFastingState(
            isFasting: isActive,
            startTime: Int(startTime.timeIntervalSince1970),
            endTime: Int(endTime.timeIntervalSince1970),
            durationHours: durationHours
        )
    }
}
