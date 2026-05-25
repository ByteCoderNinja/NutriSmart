//
//  AuthRepository.swift
//  NutriSmartIOS
//

import Foundation

class AuthRepository {
    static let shared = AuthRepository()
    
    private let api = NutriSmartApiService.shared
    private let sessionManager = SessionManager.shared
    
    private init() {}
    
    func login(request: AuthRequest) async throws -> AuthResponse {
        return try await api.login(request: request)
    }
    
    func googleLogin(request: GoogleLoginRequest) async throws -> AuthResponse {
        return try await api.googleLogin(request: request)
    }
    
    func register(request: RegisterRequest) async throws -> EmptyResponse {
        return try await api.register(request: request)
    }
    
    func verify(request: VerifyRequest) async throws -> AuthResponse {
        return try await api.verify(request: request)
    }
    
    func resendCode(email: String) async throws -> EmptyResponse {
        return try await api.resendCode(email: email)
    }
    
    func saveAuthData(authData: AuthResponse) {
        sessionManager.saveAuthToken(authData.token)
        sessionManager.saveUserId(authData.userId)
        sessionManager.saveProfileComplete(authData.isProfileComplete)
        sessionManager.saveIsVerified(authData.isVerified)
        
        UserSession.shared.token = authData.token
        UserSession.shared.currentUserId = authData.userId
    }
    
    func saveIsGoogleUser(_ isGoogle: Bool) {
        sessionManager.saveIsGoogleUser(isGoogle)
        UserSession.shared.isGoogleUser = isGoogle
    }
    
    func getAuthToken() -> String? {
        return sessionManager.fetchAuthToken()
    }
    
    func getUserId() -> Int {
        return sessionManager.fetchUserId()
    }
    
    func isProfileComplete() -> Bool {
        return sessionManager.isProfileComplete()
    }
    
    func isVerified() -> Bool {
        return sessionManager.isVerified()
    }
    
    func getWakeUpTime() -> String {
        return sessionManager.getWakeUpTime()
    }
    
    func saveWakeUpTime(_ time: String) {
        sessionManager.saveWakeUpTime(time)
    }
    
    func clearSession() {
        sessionManager.clearSession()
        UserSession.shared.clear()
    }
}
