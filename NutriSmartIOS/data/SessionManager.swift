//
//  SessionManager.swift
//  NutriSmartIOS
//
//  Created by Alex on 25/05/2026.
//

import Foundation

class SessionManager {
    static let shared = SessionManager()
    
    private let defaults = UserDefaults.standard
    
    private init() {}

    private let keyUserToken = "USER_TOKEN"
    private let keyUserId = "USER_ID"
    private let keyFastingIsActive = "FASTING_IS_ACTIVE"
    private let keyFastingStartTime = "FASTING_START_TIME"
    private let keyFastingEndTime = "FASTING_END_TIME"
    private let keyFastingDurationHours = "FASTING_DURATION_HOURS"
    private let keyWaterConsumed = "WATER_CONSUMED"
    private let keyWaterDate = "WATER_DATE"
    private let keyWakeUpTime = "wake_up_time"
    private let keyIsProfileComplete = "IS_PROFILE_COMPLETE"
    private let keyIsVerified = "IS_VERIFIED"
    private let keyIsGoogleUser = "IS_GOOGLE_USER"
    
    func saveAuthToken(_ token: String) {
        defaults.set(token, forKey: keyUserToken)
    }
    
    func fetchAuthToken() -> String? {
        return defaults.string(forKey: keyUserToken)
    }
    
    func saveUserId(_ userId: Int) {
        defaults.set(userId, forKey: keyUserId)
    }
    
    func fetchUserId() -> Int {
        return defaults.object(forKey: keyUserId) as? Int ?? -1
    }
    
    func clearSession() {
        if let bundleID = Bundle.main.bundleIdentifier {
            defaults.removePersistentDomain(forName: bundleID)
        }
    }
    
    func saveFastingState(isFasting: Bool, startTime: Int, endTime: Int, durationHours: Int) {
        defaults.set(isFasting, forKey: keyFastingIsActive)
        defaults.set(startTime, forKey: keyFastingStartTime)
        defaults.set(endTime, forKey: keyFastingEndTime)
        defaults.set(durationHours, forKey: keyFastingDurationHours)
    }
    
    func isFastingActive() -> Bool {
        return defaults.bool(forKey: keyFastingIsActive)
    }
    
    func getFastingStartTime() -> Int {
        return defaults.integer(forKey: keyFastingStartTime)
    }
    
    func getFastingEndTime() -> Int {
        return defaults.integer(forKey: keyFastingEndTime)
    }
    
    func getFastingDurationHours() -> Int {
        let duration = defaults.integer(forKey: keyFastingDurationHours)
        return duration == 0 ? 16 : duration
    }
    
    func clearFastingState() {
        defaults.removeObject(forKey: keyFastingIsActive)
        defaults.removeObject(forKey: keyFastingStartTime)
        defaults.removeObject(forKey: keyFastingEndTime)
    }
    
    func saveWaterIntake(ml: Int) {
        let today = getCurrentDateString()
        defaults.set(ml, forKey: keyWaterConsumed)
        defaults.set(today, forKey: keyWaterDate)
    }
    
    func getWaterIntake() -> Int {
        let savedDate = defaults.string(forKey: keyWaterDate) ?? ""
        let today = getCurrentDateString()
        
        if savedDate == today {
            return defaults.integer(forKey: keyWaterConsumed)
        } else {
            return 0
        }
    }

    private func getCurrentDateString() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: Date())
    }
    
    func saveWakeUpTime(_ time: String) {
        defaults.set(time, forKey: keyWakeUpTime)
    }
    
    func getWakeUpTime() -> String {
        return defaults.string(forKey: keyWakeUpTime) ?? "08:00"
    }
    
    func saveProfileComplete(_ isComplete: Bool) {
        defaults.set(isComplete, forKey: keyIsProfileComplete)
    }
    
    func isProfileComplete() -> Bool {
        return defaults.bool(forKey: keyIsProfileComplete)
    }
    
    func saveIsVerified(_ isVerified: Bool) {
        defaults.set(isVerified, forKey: keyIsVerified)
    }
    
    func isVerified() -> Bool {
        return defaults.bool(forKey: keyIsVerified)
    }
    
    func saveIsGoogleUser(_ isGoogle: Bool) {
        defaults.set(isGoogle, forKey: keyIsGoogleUser)
    }
    
    func isGoogleUser() -> Bool {
        return defaults.bool(forKey: keyIsGoogleUser)
    }
}
