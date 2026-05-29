//
//  LoginView.swift
//  NutriSmartIOS
//

import SwiftUI

struct LoginView: View {
    @State private var viewModel = LoginViewModel()
    
    var onLoginSuccess: (String, Bool, Bool) -> Void
    var onNavigateToRegister: () -> Void
    var onNavigateToForgotPassword: () -> Void
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                Spacer().frame(height: 60)
                
                VStack(spacing: 8) {
                    Image("ic_nutrismart_logo")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 100, height: 100)
                        .foregroundColor(.green)
                        .padding(.bottom, 8)
                    
                    Text("NutriSmart")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(.green)
                    
                    Text("Eat well, live smart.")
                        .font(.body)
                        .foregroundColor(.gray)
                }
                .padding(.bottom, 16)
                
                // MARK: - Inputs
                VStack(spacing: 16) {
                    TextField("Email Address", text: $viewModel.email)
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                        .padding()
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(16)
                    
                    SecureField("Password", text: $viewModel.password)
                        .padding()
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(16)
                }
                
                // MARK: - Forgot Password
                HStack {
                    Spacer()
                    Button(action: onNavigateToForgotPassword) {
                        Text("Forgot password?")
                            .font(.footnote)
                            .fontWeight(.semibold)
                            .foregroundColor(.green)
                    }
                }
                
                // MARK: - Error Message
                if let errorMessage = viewModel.errorMessage {
                    Text(errorMessage)
                        .font(.footnote)
                        .fontWeight(.medium)
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 4)
                }
                
                Spacer().frame(height: 8)
                
                // MARK: - Login Button
                Button(action: {
                    viewModel.login()
                }) {
                    ZStack {
                        if viewModel.isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("Login")
                                .font(.headline)
                                .fontWeight(.bold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(viewModel.isLoading ? Color.green.opacity(0.7) : Color.green)
                    .foregroundColor(.white)
                    .cornerRadius(24)
                }
                .disabled(viewModel.isLoading)
                
                Spacer().frame(height: 8)
                Text("OR")
                    .font(.caption)
                    .foregroundColor(.gray)
                Spacer().frame(height: 8)
                
                // MARK: - Google Button
                Button(action: {
                    // TODO: Implementează Google SignIn Launcher (vezi nota de mai jos)
                    print("Google Sign In tapped")
                }) {
                    HStack(spacing: 12) {
                        Image("ic_google_logo")
                            .resizable()
                            .frame(width: 22, height: 22)
                        
                        Text("Continue with Google")
                            .font(.headline)
                            .fontWeight(.medium)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(Color(UIColor.systemBackground))
                    .foregroundColor(Color(UIColor.label))
                    .cornerRadius(24)
                    .overlay(
                        RoundedRectangle(cornerRadius: 24)
                            .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                    )
                }
                
                Spacer().frame(height: 40)
                
                // MARK: - Register
                Button(action: onNavigateToRegister) {
                    Text("Create an account")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.green)
                }
                .padding(.bottom, 16)
            }
            .padding(24)
        }
        .background(Color(UIColor.systemBackground).edgesIgnoringSafeArea(.all))
        // Echivalent cu LaunchedEffect(viewModel.loginSuccess)
        .onChange(of: viewModel.loginSuccess) { newValue in
            if newValue {
                onLoginSuccess(viewModel.email, !viewModel.isNewUser, viewModel.isVerified)
                viewModel.resetNavigation()
            }
        }
    }
}
