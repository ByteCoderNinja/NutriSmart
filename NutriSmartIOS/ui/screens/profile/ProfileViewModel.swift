//
//  ProfileViewModel.swift
//  NutriSmartIOS
//

import Foundation
import SwiftUI

@Observable
class ProfileViewModel {
    var isLoading = true
    var user: UserDto? = nil
    var isGoogleUser = false
    var errorMessage: String? = nil
    private let userRepository = UserRepository.shared
    private let authRepository = AuthRepository.shared
    private let userId: Int
    init() { self.userId = SessionManager.shared.fetchUserId(); fetchUserData() }
    func fetchUserData() {
        guard userId != -1 else { return }
        isLoading = true
        userRepository.getUser(userId: userId) { [weak self] result in
            DispatchQueue.main.async {
                self?.isLoading = false
                switch result {
                case .success(let user): self?.user = user; self?.isGoogleUser = user.isGoogleUser
                case .failure(let error): self?.errorMessage = "Failed to load profile: \(error)"
                }
            }
        }
    }
    func logout() { UserSession.shared.clear(); authRepository.clearSession() }
}
