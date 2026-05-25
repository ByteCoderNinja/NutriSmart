//
//  MainView.swift
//  NutriSmartIOS
//

import SwiftUI

struct MainView: View {
    @State private var selectedTab = 0
    var onNavigateToLogin: () -> Void
    var onNavigateToEditPlan: () -> Void
    var onNavigateToVerifyEmail: (String) -> Void
    var onNavigateToForgotPassword: () -> Void
    
    var body: some View {
        TabView(selection: $selectedTab) {
            HomeView()
                .tabItem { Label("Home", systemImage: "house.fill") }
                .tag(0)
            FastingView()
                .tabItem { Label("Fasting", systemImage: "timer") }
                .tag(1)
            WeatherView()
                .tabItem { Label("Weather", systemImage: "cloud.sun.fill") }
                .tag(2)
            ProfileView(onNavigateToLogin: onNavigateToLogin, onNavigateToEditPlan: onNavigateToEditPlan)
                .tabItem { Label("Profile", systemImage: "person.fill") }
                .tag(3)
        }
        .tint(.nutriGreen)
    }
}
