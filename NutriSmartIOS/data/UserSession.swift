//
//  UserSession.swift
//  NutriSmartIOS
//

import Foundation

class UserSession {
    static let shared = UserSession()
    
    var currentUserId: Int = -1
    var token: String = ""
    var isGoogleUser: Bool = false

    private init() {}

    func clear() {
        currentUserId = -1
        token = ""
        isGoogleUser = false
    }
}
