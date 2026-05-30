//
//  HomeView.swift
//  NutriSmartIOS
//

import SwiftUI

struct HomeView: View {
    @State private var viewModel = HomeViewModel()
    @EnvironmentObject var router: AppRouter
    
    @State private var selectedMeal: (meal: MealDto, type: String)? = nil
    @State private var showMealDetail = false
    @State private var showMealSwap = false
    @State private var showBonusSnackSelector = false
    @State private var showShoppingList = false
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    dateHeader
                    mainStatsCard
                    macrosRow
                    mealPlanSection
                    bonusSnackSection
                    groceryListButton
                }
                .padding()
            }
            .navigationTitle("NutriSmart")
            .refreshable {
                viewModel.fetchTodayData()
            }
            .sheet(isPresented: $showMealDetail) {
                if let selected = selectedMeal {
                    MealDetailView(meal: selected.meal, mealType: selected.type, onSwapRequest: {
                        showMealDetail = false
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                            showMealSwap = true
                        }
                    })
                }
            }
            .sheet(isPresented: $showMealSwap) {
                if let selected = selectedMeal {
                    MealSwapView(viewModel: viewModel, mealType: selected.type, onSwapSuccess: {
                        viewModel.fetchTodayData()
                    })
                }
            }
            .sheet(isPresented: $showBonusSnackSelector) {
                BonusSnackSelectorView(viewModel: viewModel)
            }
            .sheet(isPresented: $showShoppingList) {
                ShoppingListView(viewModel: viewModel)
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
                    .onTapGesture {
                        selectedMeal = (breakfast, "Breakfast")
                        showMealDetail = true
                    }
            }
            if let lunch = viewModel.uiState.lunch {
                MealCard(type: "Lunch", meal: lunch, onToggle: { viewModel.toggleMeal(mealId: lunch.id, type: .LUNCH, consumed: $0) })
                    .onTapGesture {
                        selectedMeal = (lunch, "Lunch")
                        showMealDetail = true
                    }
            }
            if let dinner = viewModel.uiState.dinner {
                MealCard(type: "Dinner", meal: dinner, onToggle: { viewModel.toggleMeal(mealId: dinner.id, type: .DINNER, consumed: $0) })
                    .onTapGesture {
                        selectedMeal = (dinner, "Dinner")
                        showMealDetail = true
                    }
            }
            if let snack = viewModel.uiState.snack {
                MealCard(type: "Snack", meal: snack, onToggle: { viewModel.toggleMeal(mealId: snack.id, type: .SNACK, consumed: $0) })
                    .onTapGesture {
                        selectedMeal = (snack, "Snack")
                        showMealDetail = true
                    }
            }
        }
    }
    
    private var bonusSnackSection: some View {
        Group {
            if viewModel.uiState.burnedCalories >= 100 {
                VStack(alignment: .leading, spacing: 12) {
                    Text("Bonus Meal (Earned!)")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.nutriGreen)
                    
                    if let bonus = viewModel.uiState.bonusSnack {
                        MealCard(type: "Bonus Snack", meal: bonus, onToggle: { _ in })
                            .onTapGesture {
                                selectedMeal = (bonus, "Bonus Snack")
                                showMealDetail = true
                            }
                    } else {
                        Button(action: {
                            viewModel.loadBonusSnacks()
                            showBonusSnackSelector = true
                        }) {
                            HStack {
                                Image(systemName: "plus.circle.fill")
                                VStack(alignment: .leading) {
                                    Text("Claim your Bonus Snack!")
                                        .fontWeight(.bold)
                                    Text("You burned \(viewModel.uiState.burnedCalories) kcal. Tap to choose a treat.")
                                        .font(.caption)
                                }
                            }
                            .padding()
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color.nutriGreen.opacity(0.1))
                            .foregroundColor(.nutriGreen)
                            .cornerRadius(16)
                        }
                    }
                }
            }
        }
    }
    
    private var groceryListButton: some View {
        Button(action: { showShoppingList = true }) {
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
