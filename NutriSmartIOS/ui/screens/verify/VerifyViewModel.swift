//
//  VerifyViewModel.swift
//  NutriSmartIOS
//

import Foundation

@Observable
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
        authRepository.verify(request: request) { [weak self] result in
            DispatchQueue.main.async {
                self?.isLoading = false
                switch result {
                case .success(let authResponse):
                    self?.authRepository.saveAuthData(authData: authResponse)
                    self?.verificationSuccess = true
                case .failure(_):
                    self?.errorMessage = "Invalid or expired code"
                }
            }
        }
    }
    
    func resendCode(email: String) {
        isResending = true
        errorMessage = nil
        successMessage = nil
        
        authRepository.resendCode(email: email) { [weak self] result in
            DispatchQueue.main.async {
                self?.isResending = false
                switch result {
                case .success(_):
                    self?.successMessage = "A new code has been sent to your email"
                case .failure(_):
                    self?.errorMessage = "Failed to resend code. Try again later."
                }
            }
        }
    }
}
