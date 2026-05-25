//
//  HomeView.swift
//  NutriSmartIOS
//

import SwiftUI

struct HomeView: View {
    @State private var viewModel = HomeViewModel()
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    dateHeader
                    mainStatsCard
                    macrosRow
                    mealPlanSection
                    groceryListButton
                }
                .padding()
            }
            .navigationTitle("NutriSmart")
            .refreshable {
                viewModel.fetchTodayData()
            }
            .background(Color.backgroundLight.ignoresSafeArea())
        }
    }
    
    private var dateHeader: some View {
        VStack(alignment: .leading) {
            Text(formatDate(Date()))
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(.nutriGreen)
            Text("Today")
                .font(NutriSmartTypography.headlineMedium)
                .foregroundColor(.primary)
        }
    }
    
    private var mainStatsCard: some View {
        VStack(spacing: 16) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 12) {
                    StatItem(icon: "figure.walk", value: "\(viewModel.uiState.steps)", label: "Steps", color: .nutriBlue)
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Weight")
                            .font(.system(size: 12))
                            .foregroundColor(.secondary)
                        HStack {
                            Button(action: { viewModel.adjustWeight(delta: -0.1) }) {
                                Image(systemName: "minus.circle.fill").foregroundColor(.nutriGreen)
                            }
                            Text(String(format: "%.1f %@", viewModel.uiState.weight, viewModel.uiState.isImperial ? "lbs" : "kg"))
                                .font(.system(size: 16, weight: .bold))
                            Button(action: { viewModel.adjustWeight(delta: 0.1) }) {
                                Image(systemName: "plus.circle.fill").foregroundColor(.nutriGreen)
                            }
                        }
                    }
                }
                Spacer()
                VStack(spacing: 8) {
                    Text("Water Intake").font(.system(size: 14, weight: .bold))
                    ZStack {
                        Circle().stroke(Color.nutriBlue.opacity(0.2), lineWidth: 8)
                        Circle()
                            .trim(from: 0, to: CGFloat(min(1, Double(viewModel.uiState.waterConsumedMl) / Double(viewModel.uiState.waterGoalMl))))
                            .stroke(Color.nutriBlue, style: StrokeStyle(lineWidth: 8, lineCap: .round))
                            .rotationEffect(.degrees(-90))
                        VStack {
                            Text("\(viewModel.uiState.waterConsumedMl)").font(.system(size: 18, weight: .bold))
                            Text("ml").font(.system(size: 12))
                        }
                    }
                    .frame(width: 80, height: 80)
                    HStack {
                        Button(action: { viewModel.removeWater() }) { Image(systemName: "minus.circle") }
                        Button(action: { viewModel.addWater() }) { Image(systemName: "plus.circle") }
                    }
                    .foregroundColor(.nutriBlue).font(.title2)
                }
            }
        }
        .padding()
        .background(Color.white)
        .cornerRadius(20)
        .shadow(color: Color.black.opacity(0.05), radius: 10, x: 0, y: 5)
    }
    
    private var macrosRow: some View {
        HStack {
            MacroItem(label: "Protein", value: "\(totalProtein)g", color: .nutriOrange)
            Spacer()
            MacroItem(label: "Carbs", value: "\(totalCarbs)g", color: .nutriBlue)
            Spacer()
            MacroItem(label: "Fat", value: "\(totalFat)g", color: .nutriGreen)
        }
    }
    
    private var mealPlanSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Today's Meal Plan").font(.system(size: 20, weight: .bold))
            if let breakfast = viewModel.uiState.breakfast {
                MealCard(type: "Breakfast", meal: breakfast, onToggle: { viewModel.toggleMeal(mealId: breakfast.id, type: .BREAKFAST, consumed: $0) })
            }
            if let lunch = viewModel.uiState.lunch {
                MealCard(type: "Lunch", meal: lunch, onToggle: { viewModel.toggleMeal(mealId: lunch.id, type: .LUNCH, consumed: $0) })
            }
            if let dinner = viewModel.uiState.dinner {
                MealCard(type: "Dinner", meal: dinner, onToggle: { viewModel.toggleMeal(mealId: dinner.id, type: .DINNER, consumed: $0) })
            }
            if let snack = viewModel.uiState.snack {
                MealCard(type: "Snack", meal: snack, onToggle: { viewModel.toggleMeal(mealId: snack.id, type: .SNACK, consumed: $0) })
            }
        }
    }
    
    private var groceryListButton: some View {
        Button(action: { /* Show Shopping List */ }) {
            HStack {
                Image(systemName: "cart.fill")
                Text("View Grocery List").fontWeight(.bold)
            }
            .frame(maxWidth: .infinity).frame(height: 56)
            .background(Color.nutriGreen).foregroundColor(.white).cornerRadius(16)
        }
    }
    
    private var totalProtein: Int { (viewModel.uiState.breakfast?.protein ?? 0) + (viewModel.uiState.lunch?.protein ?? 0) + (viewModel.uiState.dinner?.protein ?? 0) + (viewModel.uiState.snack?.protein ?? 0) }
    private var totalCarbs: Int { (viewModel.uiState.breakfast?.carbs ?? 0) + (viewModel.uiState.lunch?.carbs ?? 0) + (viewModel.uiState.dinner?.carbs ?? 0) + (viewModel.uiState.snack?.carbs ?? 0) }
    private var totalFat: Int { (viewModel.uiState.breakfast?.fat ?? 0) + (viewModel.uiState.lunch?.fat ?? 0) + (viewModel.uiState.dinner?.fat ?? 0) + (viewModel.uiState.snack?.fat ?? 0) }
    
    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .long
        return formatter.string(from: date)
    }
}

struct StatItem: View {
    let icon: String
    let value: String
    let label: String
    let color: Color
    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon).foregroundColor(color)
            VStack(alignment: .leading) {
                Text(value).font(.system(size: 16, weight: .bold))
                Text(label).font(.system(size: 12)).foregroundColor(.secondary)
            }
        }
    }
}

struct MacroItem: View {
    let label: String
    let value: String
    let color: Color
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label).font(.system(size: 12)).foregroundColor(.secondary)
            Text(value).font(.system(size: 16, weight: .bold)).foregroundColor(color)
        }
        .padding(.horizontal, 16).padding(.vertical, 8)
        .background(color.opacity(0.1)).cornerRadius(12)
    }
}

struct MealCard: View {
    let type: String
    let meal: MealDto
    let onToggle: (Bool) -> Void
    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(type).font(.system(size: 12, weight: .bold)).foregroundColor(.nutriGreen)
                Text(meal.name).font(.system(size: 16, weight: .semibold))
                Text("\(meal.calories) kcal | P:\(meal.protein) C:\(meal.carbs) F:\(meal.fat)").font(.system(size: 12)).foregroundColor(.secondary)
            }
            Spacer()
            Button(action: { onToggle(!meal.consumed) }) {
                Image(systemName: meal.consumed ? "checkmark.circle.fill" : "circle").font(.title2).foregroundColor(meal.consumed ? .nutriGreen : .gray)
            }
        }
        .padding().background(Color.white).cornerRadius(16).shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
    }
}
