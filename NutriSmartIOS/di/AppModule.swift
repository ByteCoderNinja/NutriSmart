//
//  AppModule.swift
//  NutriSmartIOS
//
//  Created by Alex on 30/05/2026.
//

import Foundation

final class AppModule {
    static let shared = AppModule()
    
    let sessionManager = SessionManager.shared
    let apiService = NutriSmartApiService.shared
    
    private init() {
    }
}
