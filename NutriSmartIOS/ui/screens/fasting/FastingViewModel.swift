//
//  FastingViewModel.swift
//  NutriSmartIOS
//

import Foundation
import SwiftUI

@Observable
class FastingViewModel {
    var isFasting = false
    var selectedDurationHours = 16
    var endTime: Date? = nil
    var startTime: Date? = nil
    var progress: Double = 0.0
    var timeRemainingString = "00:00:00"
    
    private let fastingRepository = FastingRepository.shared
    private var timer: Timer? = nil
    
    init() {
        restoreFastingState()
        startTimer()
    }
    
    private func restoreFastingState() {
        let isActive = fastingRepository.isFastingActive()
        if isActive {
            let end = fastingRepository.getFastingEndTime()
            if Date() >= end {
                isFasting = false
                progress = 1.0
                timeRemainingString = "00:00:00"
                selectedDurationHours = fastingRepository.getFastingDurationHours()
                fastingRepository.clearFastingState()
            } else {
                isFasting = true
                startTime = fastingRepository.getFastingStartTime()
                endTime = end
                selectedDurationHours = fastingRepository.getFastingDurationHours()
            }
        } else {
            selectedDurationHours = fastingRepository.getFastingDurationHours()
        }
    }
    
    func selectDuration(_ hours: Int) {
        if !isFasting {
            selectedDurationHours = hours
        }
    }
    
    func toggleFasting() {
        if isFasting {
            isFasting = false
            progress = 0.0
            timeRemainingString = "00:00:00"
            fastingRepository.clearFastingState()
            timer?.invalidate()
        } else {
            let durationSeconds = Double(selectedDurationHours * 3600)
            let start = Date()
            let end = start.addingTimeInterval(durationSeconds)
            
            isFasting = true
            startTime = start
            endTime = end
            
            fastingRepository.saveFastingState(
                isActive: true,
                startTime: start,
                endTime: end,
                durationHours: selectedDurationHours
            )
            startTimer()
        }
    }
    
    private func startTimer() {
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            self?.updateProgress()
        }
        updateProgress()
    }
    
    private func updateProgress() {
        guard isFasting, let endTime = endTime, let startTime = startTime else { return }
        
        let currentTime = Date()
        let timeRemaining = endTime.timeIntervalSince(currentTime)
        let totalDuration = Double(selectedDurationHours * 3600)
        
        if timeRemaining <= 0 {
            isFasting = false
            progress = 1.0
            timeRemainingString = "00:00:00"
            timer?.invalidate()
        } else {
            progress = 1.0 - (timeRemaining / totalDuration)
            
            let hours = Int(timeRemaining) / 3600
            let minutes = (Int(timeRemaining) % 3600) / 60
            let seconds = Int(timeRemaining) % 60
            
            timeRemainingString = String(format: "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}
