//
//  RegisterView.swift
//  NutriSmartIOS
//

import SwiftUI

struct RegisterView: View {
    @State private var viewModel = RegisterViewModel()
    
    var onRegisterSuccess: (String) -> Void
    var onNavigateToLogin: () -> Void
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                Spacer().frame(height: 40)
                
                Text("Create Account")
                    .font(NutriSmartTypography.headlineMedium)
                    .foregroundColor(.nutriGreen)
                
                Text("Start your journey to a healthier you.")
                    .font(NutriSmartTypography.bodyMedium)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                
                VStack(spacing: 16) {
                    CustomTextField(value: $viewModel.username, placeholder: "Username")
                    CustomTextField(value: $viewModel.email, placeholder: "Email", keyboardType: .emailAddress)
                    CustomSecureField(value: $viewModel.password, placeholder: "Password")
                }
                
                if let error = viewModel.errorMessage {
                    Text(error)
                        .font(.system(size: 14))
                        .foregroundColor(.red)
                }
                
                Button(action: { viewModel.register() }) {
                    ZStack {
                        if viewModel.isLoading {
                            ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("Register")
                                .font(NutriSmartTypography.labelLarge)
                                .fontWeight(.bold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(Color.nutriGreen)
                    .foregroundColor(.white)
                    .cornerRadius(24)
                }
                .disabled(viewModel.isLoading)
                
                Button(action: onNavigateToLogin) {
                    HStack {
                        Text("Already have an account?")
                            .font(NutriSmartTypography.bodyMedium)
                            .foregroundColor(.secondary)
                        Text("Login")
                            .font(NutriSmartTypography.bodyMedium)
                            .fontWeight(.bold)
                            .foregroundColor(.nutriGreen)
                    }
                }
                .padding(.top, 8)
            }
            .padding(24)
        }
        .background(Color.backgroundLight.ignoresSafeArea())
        .onChange(of: viewModel.navigateToVerify) { _, navigate in
            if navigate {
                onRegisterSuccess(viewModel.email)
            }
        }
    }
}
