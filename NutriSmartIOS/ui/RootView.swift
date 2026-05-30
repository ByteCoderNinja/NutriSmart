//
//  RootView.swift
//  NutriSmartIOS
//
//  Created by Alex on 25/05/2026.
//

import SwiftUI

struct RootView: View {
    @StateObject private var router = AppRouter()
    
    var body: some View {
        Group {
            switch router.currentRoot {
            case .login:
                NavigationStack(path: $router.authPath) {
                    LoginView(
                        onLoginSuccess: { email, profileComplete, verified in
                            if !verified {
                                router.authPath.append(AuthRoute.verify(email: email, isEdit: false))
                            } else if !profileComplete {
                                router.switchRoot(to: .onboarding)
                            } else {
                                router.switchRoot(to: .main)
                            }
                        },
                        onNavigateToRegister: { router.authPath.append(AuthRoute.register) },
                        onNavigateToForgotPassword: { router.authPath.append(AuthRoute.forgotPassword) }
                    )
                    .navigationDestination(for: AuthRoute.self) { route in
                        switch route {
                        case .register:
                            RegisterView(
                                onRegisterSuccess: { email in
                                    router.authPath.append(AuthRoute.verify(email: email, isEdit: false))
                                },
                                onNavigateToLogin: { router.authPath.removeLast() }
                            )
                        case .verify(let email, let isEdit):
                            VerifyView(
                                email: email,
                                onVerificationSuccess: {
                                    if isEdit {
                                        router.authPath.removeLast()
                                    } else {
                                        router.switchRoot(to: .onboarding)
                                    }
                                }
                            )
                        case .forgotPassword:
                            // TODO: Replace with ForgotPasswordView when ready
                            Text("Forgot Password Screen")
                        case .resetPassword(let email):
                            // TODO: Replace with ResetPasswordView when ready
                            Text("Reset Password Screen: \(email)")
                        default:
                            Text("Route not implemented yet")
                        }
                    }
                }
                
            case .onboarding:
                NavigationStack {
                    OnboardingView(
                        isEditMode: false,
                        onProfileComplete: {
                            router.switchRoot(to: .main)
                        }
                    )
                }
                
            case .main:
                NavigationStack(path: $router.mainPath) {
                    MainView(
                        onNavigateToLogin: { router.switchRoot(to: .login) },
                        onNavigateToEditPlan: { router.mainPath.append(.editPlan) },
                        onNavigateToVerifyEmail: { email in
                            router.mainPath.append(.verify(email: email, isEdit: true))
                        },
                        onNavigateToForgotPassword: { router.mainPath.append(.forgotPassword) }
                    )
                    .navigationDestination(for: MainRoute.self) { route in
                        switch route {
                        case .editPlan:
                            OnboardingView(
                                isEditMode: true,
                                onProfileComplete: { router.mainPath.removeLast() }
                            )
                        case .verify(let email, let isEdit):
                            VerifyView(
                                email: email,
                                onVerificationSuccess: { router.mainPath.removeLast() }
                            )
                        case .forgotPassword:
                            Text("Forgot Password (Logged In)")
                        case .shoppingList:
                            // TODO: Replace with ShoppingListView when ready
                            Text("Shopping List Screen")
                        }
                    }
                }
            }
        }
        .environmentObject(router)
    }
}
