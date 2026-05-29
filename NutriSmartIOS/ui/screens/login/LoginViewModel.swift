//
//  LoginViewModel.swift
//  NutriSmartIOS
//

import Foundation
import SwiftUI
import Combine

@Observable
@MainActor
class LoginViewModel {
    var email = ""
    var password = ""
    var isLoading = false
    var errorMessage: String? = nil
    
    var loginSuccess = false
    var isNewUser = false
    var isVerified = true
    
    private let authRepository = AuthRepository.shared
    
    func resetNavigation() {
        loginSuccess = false
    }
    
    func onEmailChange(_ newEmail: String) {
        email = newEmail
    }
    
    func onPasswordChange(_ newPassword: String) {
        password = newPassword
    }
    
    func login() {
            isLoading = true
            errorMessage = nil
            
            Task {
                do {
                    let request = AuthRequest(email: email, password: password)
                    let response = try await authRepository.login(request: request)
                    
                    UserSession.shared.currentUserId = response.userId
                    UserSession.shared.token = response.token
                    
                    authRepository.saveAuthData(authData: response)
                    SessionManager.shared.saveIsGoogleUser(false)
                    
                    isNewUser = !response.isProfileComplete
                    isVerified = response.isVerified
                    loginSuccess = true
                    
                } catch {
                    self.errorMessage = "Login failed: \(error.localizedDescription)"
                }
                isLoading = false
            }
        }
        
        func loginWithGoogle(idToken: String) {
            isLoading = true
            errorMessage = nil
            
            Task {
                do {
                    let request = GoogleLoginRequest(token: idToken)
                    let response = try await authRepository.googleLogin(request: request)
                    
                    UserSession.shared.currentUserId = response.userId
                    UserSession.shared.token = response.token
                    
                    authRepository.saveAuthData(authData: response)
                    SessionManager.shared.saveIsGoogleUser(true)
                    
                    isNewUser = !response.isProfileComplete
                    isVerified = response.isVerified
                    loginSuccess = true
                    
                } catch {
                    self.errorMessage = "Google login failed: \(error.localizedDescription)"
                }
                isLoading = false
            }
        }
}
