//
//  RegisterViewModel.swift
//  NutriSmartIOS
//

import Foundation

@Observable
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
        authRepository.register(request: request) { [weak self] result in
            DispatchQueue.main.async {
                self?.isLoading = false
                switch result {
                case .success(_):
                    self?.navigateToVerify = true
                case .failure(let error):
                    self?.errorMessage = "Registration failed. Email might be in use."
                }
            }
        }
    }
}
