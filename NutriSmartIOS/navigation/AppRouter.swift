//
//  AppRouter.swift
//  NutriSmartIOS
//
//  Created by Alex on 25/05/2026.
//

import SwiftUI
import Combine

enum AppRoot {
    case login
    case onboarding
    case main
}

enum AuthRoute: Hashable {
    case login
    case register
    case verify(email: String, isEdit: Bool)
    case forgotPassword
    case resetPassword(email: String)
}

enum MainRoute: Hashable {
    case editPlan
    case verify(email: String, isEdit: Bool)
    case forgotPassword
    case shoppingList
}

class AppRouter: ObservableObject {
    @Published var currentRoot: AppRoot = .login
    @Published var authPath: [AuthRoute] = []
    @Published var mainPath: [MainRoute] = []
    init() {
        determineStartDestination()
    }
    
    func determineStartDestination() {
        let session = SessionManager.shared
        
        if session.fetchAuthToken() != nil {
            if !session.isVerified() {
                currentRoot = .login
            } else if !session.isProfileComplete() {
                currentRoot = .onboarding
            } else {
                currentRoot = .main
            }
        } else {
            currentRoot = .login
        }
    }
    
    func switchRoot(to newRoot: AppRoot) {
        authPath = []
        mainPath = []
        currentRoot = newRoot
    }
}
