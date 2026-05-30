//
//  HealthKitManager.swift
//  NutriSmartIOS
//

import Foundation
import HealthKit

class HealthKitManager {
    static let shared = HealthKitManager()
    private let healthStore = HKHealthStore()
    
    private init() {}
    
    func requestAuthorization() async -> Bool {
        guard HKHealthStore.isHealthDataAvailable() else { return false }
        
        let typesToRead: Set = [
            HKObjectType.quantityType(forIdentifier: .stepCount)!,
            HKObjectType.quantityType(forIdentifier: .bodyMass)!
        ]
        
        do {
            try await healthStore.requestAuthorization(toShare: [], read: typesToRead)
            return true
        } catch {
            print("HealthKit Authorization failed: \(error)")
            return false
        }
    }
    
    func getTodaySteps() async -> Int {
        guard HKHealthStore.isHealthDataAvailable() else { return 0 }
        
        let stepsType = HKQuantityType.quantityType(forIdentifier: .stepCount)!
        let now = Date()
        let startOfDay = Calendar.current.startOfDay(for: now)
        let predicate = HKQuery.predicateForSamples(withStart: startOfDay, end: now, options: .strictStartDate)
        
        return await withCheckedContinuation { continuation in
            let query = HKStatisticsQuery(quantityType: stepsType, quantitySamplePredicate: predicate, options: .cumulativeSum) { _, result, error in
                guard let result = result, let sum = result.sumQuantity() else {
                    continuation.resume(returning: 0)
                    return
                }
                continuation.resume(returning: Int(sum.doubleValue(for: HKUnit.count())))
            }
            healthStore.execute(query)
        }
    }
    
    func getLatestWeight() async -> Double? {
        guard HKHealthStore.isHealthDataAvailable() else { return nil }
        
        let weightType = HKQuantityType.quantityType(forIdentifier: .bodyMass)!
        let sortDescriptor = NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: false)
        
        return await withCheckedContinuation { continuation in
            let query = HKSampleQuery(sampleType: weightType, predicate: nil, limit: 1, sortDescriptors: [sortDescriptor]) { _, results, error in
                guard let sample = results?.first as? HKQuantitySample else {
                    continuation.resume(returning: nil)
                    return
                }
                // Convert to kg by default, or lbs if needed. We'll return kg and let UI handle conversion if necessary.
                continuation.resume(returning: sample.quantity.doubleValue(for: HKUnit.gramUnit(with: .kilo)))
            }
            healthStore.execute(query)
        }
    }
}
