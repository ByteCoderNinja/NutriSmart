//
//  ChangePasswordView.swift
//  NutriSmartIOS
//

import SwiftUI

struct ChangePasswordView: View {
    @State private var oldPassword = ""
    @State private var newPassword = ""
    @State private var confirmPassword = ""
    
    @Environment(\.dismiss) var dismiss
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                VStack(spacing: 12) {
                    SecureField("Current Password", text: $oldPassword)
                    SecureField("New Password", text: $newPassword)
                    SecureField("Confirm New Password", text: $confirmPassword)
                }
                .padding()
                .background(Color.white)
                .cornerRadius(12)
                .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                .padding(.horizontal)
                
                if let error = errorMessage {
                    Text(error)
                        .foregroundColor(.red)
                        .font(.caption)
                }
                
                Spacer()
                
                Button(action: {
                    if newPassword != confirmPassword {
                        errorMessage = "Passwords do not match"
                        return
                    }
                    // Implement password change logic here if repository supports it
                    // For now, we'll just show a success message and close
                    Task {
                        isLoading = true
                        try? await Task.sleep(nanoseconds: 1_000_000_000)
                        dismiss()
                    }
                }) {
                    if isLoading {
                        ProgressView().tint(.white)
                    } else {
                        Text("Update Password")
                            .fontWeight(.bold)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 56)
                .background(Color.nutriGreen)
                .foregroundColor(.white)
                .cornerRadius(16)
                .padding()
                .disabled(isLoading || oldPassword.isEmpty || newPassword.isEmpty)
            }
            .navigationTitle("Change Password")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { dismiss() }
                }
            }
            .background(Color.backgroundLight.ignoresSafeArea())
        }
    }
}
