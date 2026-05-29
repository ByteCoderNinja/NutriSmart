//
//  ProfileViewModel.swift
//  NutriSmartIOS
//

import Foundation
import SwiftUI

@Observable
@MainActor
class ProfileViewModel {
    var isLoading = true
    var user: UserDto? = nil
    var isGoogleUser = false
    var errorMessage: String? = nil
    
    private let userRepository = UserRepository.shared
    private let authRepository = AuthRepository.shared
    private let userId: Int
    
    init() {
        self.userId = SessionManager.shared.fetchUserId()
        fetchUserData()
    }
    
    func fetchUserData() {
        guard userId != -1 else { return }
        isLoading = true
        
        let token = SessionManager.shared.fetchAuthToken() ?? ""
        
        Task {
            do {
                let user = try await userRepository.getUser(token: token, userId: userId)
                self.user = user
                self.isGoogleUser = user.isGoogleUser
                self.isLoading = false
            } catch {
                self.errorMessage = "Failed to load profile: \(error)"
                self.isLoading = false
            }
        }
    }
    
    func logout() {
        SessionManager.shared.clearSession()
        authRepository.clearSession()
    }
}
