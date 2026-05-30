//
//  EatingReminder.swift
//  NutriSmartIOS
//

import Foundation
import UserNotifications

class NotificationManager {
    static let shared = NotificationManager()
    
    private init() {}
    
    func requestAuthorization() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, error in
            if granted {
                print("Notification permission granted.")
            } else if let error = error {
                print("Notification permission denied: \(error.localizedDescription)")
            }
        }
    }
    
    func scheduleMealNotification(mealType: String, date: Date, isReminder: Bool) {
        let content = UNMutableNotificationContent()
        content.title = isReminder ? "Meal Reminder" : "Time to eat!"
        content.body = "It's time for your \(mealType.lowercased()). Stay on track with your plan!"
        content.sound = .default
        
        let calendar = Calendar.current
        let components = calendar.dateComponents([.hour, .minute], from: date)
        
        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: true)
        
        let identifier = "\(mealType)_\(isReminder ? "reminder" : "base")"
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("Error scheduling notification: \(error.localizedDescription)")
            }
        }
    }
    
    func cancelMealNotification(mealType: String, isReminder: Bool) {
        let identifier = "\(mealType)_\(isReminder ? "reminder" : "base")"
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [identifier])
    }
}
