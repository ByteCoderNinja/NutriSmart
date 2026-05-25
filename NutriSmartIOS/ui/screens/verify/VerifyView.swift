//
//  VerifyView.swift
//  NutriSmartIOS
//

import SwiftUI

struct VerifyView: View {
    var email: String
    var onVerificationSuccess: () -> Void
    
    @State private var viewModel = VerifyViewModel()
    
    var body: some View {
        VStack(spacing: 24) {
            Spacer().frame(height: 40)
            
            Text("Verify Email")
                .font(NutriSmartTypography.headlineMedium)
                .foregroundColor(.nutriGreen)
            
            Text("Enter the verification code sent to\n\(email)")
                .font(NutriSmartTypography.bodyMedium)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
            
            CustomTextField(value: $viewModel.code, placeholder: "Verification Code", keyboardType: .numberPad)
                .multilineTextAlignment(.center)
                .font(.system(size: 24, weight: .bold))
            
            if let error = viewModel.errorMessage {
                Text(error)
                    .font(.system(size: 14))
                    .foregroundColor(.red)
            }
            
            if let success = viewModel.successMessage {
                Text(success)
                    .font(.system(size: 14))
                    .foregroundColor(.nutriGreen)
            }
            
            Button(action: { viewModel.verify(email: email) }) {
                ZStack {
                    if viewModel.isLoading {
                        ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("Verify")
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
            
            Button(action: { viewModel.resendCode(email: email) }) {
                if viewModel.isResending {
                    ProgressView()
                } else {
                    Text("Resend Code")
                        .font(NutriSmartTypography.bodyMedium)
                        .fontWeight(.bold)
                        .foregroundColor(.nutriGreen)
                }
            }
            
            Spacer()
        }
        .padding(24)
        .background(Color.backgroundLight.ignoresSafeArea())
        .onChange(of: viewModel.verificationSuccess) { _, success in
            if success {
                onVerificationSuccess()
            }
        }
    }
}
