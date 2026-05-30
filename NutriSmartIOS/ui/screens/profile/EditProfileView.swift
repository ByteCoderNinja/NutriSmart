//
//  EditProfileView.swift
//  NutriSmartIOS
//

import SwiftUI

struct EditProfileView: View {
    let title: String
    @State var value: String
    let onSave: (String) async -> Bool
    
    @Environment(\.dismiss) var dismiss
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                TextField(title, text: $value)
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
                    Task {
                        isLoading = true
                        if await onSave(value) {
                            dismiss()
                        } else {
                            errorMessage = "Failed to update \(title.lowercased())"
                        }
                        isLoading = false
                    }
                }) {
                    if isLoading {
                        ProgressView().tint(.white)
                    } else {
                        Text("Save Changes")
                            .fontWeight(.bold)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 56)
                .background(Color.nutriGreen)
                .foregroundColor(.white)
                .cornerRadius(16)
                .padding()
                .disabled(isLoading || value.isEmpty)
            }
            .navigationTitle("Edit \(title)")
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
