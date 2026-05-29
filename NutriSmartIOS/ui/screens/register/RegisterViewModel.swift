//
//  RegisterViewModel.swift
//  NutriSmartIOS
//

import Foundation

@Observable
@MainActor
class RegisterViewModel {
    var email = ""
    var username = ""
    var password = ""
    
    var isLoading = false
    var errorMessage: String? = nil
    var navigateToVerify = false
    
    private let authRepository = AuthRepository.shared
    
    func register() {
        isLoading = true
        errorMessage = nil
        
        let request = RegisterRequest(email: email, password: password, username: username)
        Task {
            do {
                _ = try await authRepository.register(request: request)
                self.navigateToVerify = true
                self.isLoading = false
            } catch {
                self.errorMessage = "Registration failed. Email might be in use."
                self.isLoading = false
            }
        }
    }
}
