//
//  UserRepository.swift
//  NutriSmartIOS
//

import Foundation

class UserRepository {
    static let shared = UserRepository()
    
    private let api = NutriSmartApiService.shared
    
    private init() {}
    
    func getUser(token: String, userId: Int) async throws -> UserDto {
        return try await api.getUser(token: token, userId: userId)
    }
    
    func patchUser(token: String, userId: Int, request: UpdateUserRequest) async throws -> UserDto {
        return try await api.patchUser(token: token, userId: userId, request: request)
    }
    
    func deleteUser(token: String, userId: Int) async throws -> EmptyResponse {
        return try await api.deleteUser(token: token, userId: userId)
    }
    
    func completeProfile(token: String, request: OnboardingRequest) async throws -> EmptyResponse {
        return try await api.completeProfile(token: token, request: request)
    }
    
    func startPlanGeneration(token: String, userId: Int) async throws -> [String: String] {
        return try await api.startPlanGeneration(token: token, userId: userId)
    }
    
    func checkGenerationStatus(token: String, userId: Int) async throws -> [String: String] {
        return try await api.checkGenerationStatus(token: token, userId: userId)
    }
    
    func searchFoods(token: String, query: String) async throws -> [String] {
        return try await api.searchFoods(token: token, query: query)
    }
}
