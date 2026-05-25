//
//  NetworkManager.swift
//  NutriSmartIOS
//
//  Created by Alex on 19/05/2026.
//

import Foundation

enum NetworkError: Error {
    case invalidURL
    case noData
    case decodingError
    case serverError(String)
}

class NetworkManager {
    static let shared = NetworkManager()
    private let session: URLSession
    
    private let baseURL = "https://nutrismart-91no.onrender.com/api/"
    
    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30.0
        config.timeoutIntervalForResource = 30.0
        
        self.session = URLSession(configuration: config)
    }
    
    func request<T: Codable>(endpoint: String, method: String = "GET", body: Codable? = nil, token: String? = nil) async throws -> T {
        guard let url = URL(string: "\(baseURL)\(endpoint)") else {
            throw NetworkError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        if let token = token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        if let body = body {
            request.httpBody = try? JSONEncoder().encode(body)
        }
        
        let (data, response) = try await session.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
            throw NetworkError.serverError("Cod eroare: \((response as? HTTPURLResponse)?.statusCode ?? 0)")
        }
        
        return try JSONDecoder().decode(T.self, from: data)
    }
}
