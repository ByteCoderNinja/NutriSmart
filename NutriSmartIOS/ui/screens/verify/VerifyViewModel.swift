//
//  VerifyViewModel.swift
//  NutriSmartIOS
//

import Foundation

@Observable
@MainActor
class VerifyViewModel {
    var code = ""
    var isLoading = false
    var isResending = false
    var errorMessage: String? = nil
    var successMessage: String? = nil
    var verificationSuccess = false
    
    private let authRepository = AuthRepository.shared
    
    func verify(email: String) {
        isLoading = true
        errorMessage = nil
        successMessage = nil
        
        let request = VerifyRequest(email: email, code: code)
        Task {
            do {
                let authResponse = try await authRepository.verify(request: request)
                authRepository.saveAuthData(authData: authResponse)
                self.verificationSuccess = true
                self.isLoading = false
            } catch {
                self.errorMessage = "Invalid or expired code"
                self.isLoading = false
            }
        }
    }
    
    func resendCode(email: String) {
        isResending = true
        errorMessage = nil
        successMessage = nil
        
        Task {
            do {
                _ = try await authRepository.resendCode(email: email)
                self.successMessage = "A new code has been sent to your email"
                self.isResending = false
            } catch {
                self.errorMessage = "Failed to resend code. Try again later."
                self.isResending = false
            }
        }
    }
}
