//
//  ProfileView.swift
//  NutriSmartIOS
//

import SwiftUI

struct ProfileView: View {
    @State private var viewModel = ProfileViewModel()
    var onNavigateToLogin: () -> Void
    var onNavigateToEditPlan: () -> Void
    var body: some View {
        NavigationStack {
            ScrollView {
                if viewModel.isLoading { ProgressView().padding() }
                else if let user = viewModel.user {
                    VStack(spacing: 24) {
                        VStack(spacing: 16) {
                            ZStack { Circle().fill(Color.nutriGreen.opacity(0.1)).frame(width: 100, height: 100); Text(user.username.prefix(1).uppercased()).font(.system(size: 40, weight: .bold)).foregroundColor(.nutriGreen) }
                            VStack(spacing: 4) { Text(user.username).font(.system(size: 24, weight: .bold)); Text(user.email).font(.system(size: 14)).foregroundColor(.secondary) }
                        }
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Current Stats").font(.system(size: 18, weight: .bold))
                            HStack(spacing: 12) { MiniStatCard(icon: "scalemass.fill", value: "\(user.weight ?? 0)", unit: user.isImperial ? "lbs" : "kg"); MiniStatCard(icon: "ruler.fill", value: "\(user.height ?? 0)", unit: user.isImperial ? "ft" : "cm"); MiniStatCard(icon: "calendar", value: calculateAge(user.dateOfBirth), unit: "years") }
                        }
                        VStack(alignment: .leading, spacing: 12) {
                            Text("My Goals").font(.system(size: 18, weight: .bold))
                            HStack(spacing: 12) { MiniStatCard(icon: "star.fill", value: "\(user.targetWeight ?? 0)", unit: "target"); MiniStatCard(icon: "figure.walk", value: "\(user.stepGoal ?? 10000)", unit: "steps/day") }
                        }
                        Spacer().frame(height: 20)
                        Button(action: onNavigateToEditPlan) { Text("Edit AI Plan").font(.system(size: 16, weight: .bold)).frame(maxWidth: .infinity).padding().background(Color.nutriGreen.opacity(0.1)).foregroundColor(.nutriGreen).cornerRadius(12) }
                        Button(action: { viewModel.logout(); onNavigateToLogin() }) { Text("Logout").font(.system(size: 16, weight: .bold)).foregroundColor(.red).padding() }
                    }.padding(24)
                } else { Text("Error loading profile") }
            }.navigationTitle("Profile").refreshable { viewModel.fetchUserData() }.background(Color.backgroundLight.ignoresSafeArea())
        }
    }
    private func calculateAge(_ birthDate: String?) -> String {
        guard let birthDate = birthDate, let date = { let f = DateFormatter(); f.dateFormat = "yyyy-MM-dd"; return f.date(from: birthDate) }() else { return "--" }
        return "\(Calendar.current.dateComponents([.year], from: date, to: Date()).year ?? 0)"
    }
}

struct MiniStatCard: View {
    let icon: String; let value: String; let unit: String
    var body: some View {
        VStack(spacing: 8) { Image(systemName: icon).foregroundColor(.nutriGreen); Text(value).font(.system(size: 18, weight: .bold)); Text(unit).font(.system(size: 12)).foregroundColor(.secondary) }
        .frame(maxWidth: .infinity).padding(.vertical, 16).background(Color.white).cornerRadius(16).shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
    }
}
