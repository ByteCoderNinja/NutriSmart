//
//  SettingsView.swift
//  NutriSmartIOS
//

import SwiftUI

struct SettingsView: View {
    @State private var viewModel = ProfileViewModel()
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationStack {
            List {
                Section(header: Text("Account")) {
                    NavigationLink("Edit Profile") {
                        EditProfileView(title: "Username", value: viewModel.user?.username ?? "", onSave: { newValue in
                            await viewModel.updateProfile(username: newValue)
                        })
                    }
                    NavigationLink("Change Password") {
                        ChangePasswordView()
                    }
                }
                
                Section(header: Text("Preferences")) {
                    Toggle("Imperial Units", isOn: Binding(
                        get: { viewModel.user?.isImperial ?? false },
                        set: { newValue in
                            Task { await viewModel.updateProfile() /* Update imperial logic */ }
                        }
                    ))
                }
                
                Section(header: Text("Notifications")) {
                    Button("Request Permissions") {
                        NotificationManager.shared.requestAuthorization()
                    }
                }
                
                Section {
                    Button("Logout", role: .destructive) {
                        viewModel.logout()
                        // This will trigger root switch via AppRouter if managed correctly
                    }
                }
            }
            .navigationTitle("Settings")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }
}
