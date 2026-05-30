//
//  MealDetailView.swift
//  NutriSmartIOS
//

import SwiftUI

struct MealDetailView: View {
    let meal: MealDto
    let mealType: String
    var onSwapRequest: () -> Void
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 20)
                            .fill(Color.nutriGreen.opacity(0.1))
                            .frame(height: 200)
                        Image(systemName: "fork.knife").font(.system(size: 60)).foregroundColor(.nutriGreen)
                    }
                    VStack(alignment: .leading, spacing: 8) {
                        Text(mealType.capitalized).font(.subheadline).fontWeight(.bold).foregroundColor(.nutriGreen)
                        Text(meal.name).font(.title).fontWeight(.bold)
                    }
                    HStack(spacing: 16) {
                        MacroDetailItem(label: "Calories", value: "\(meal.calories)", unit: "kcal", color: .nutriGreen)
                        MacroDetailItem(label: "Protein", value: "\(meal.protein)", unit: "g", color: .nutriOrange)
                        MacroDetailItem(label: "Carbs", value: "\(meal.carbs)", unit: "g", color: .nutriBlue)
                        MacroDetailItem(label: "Fat", value: "\(meal.fat)", unit: "g", color: .nutriGreen)
                    }
                    if let details = meal.quantityDetails {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Ingredients & Portions").font(.headline)
                            Text(details).font(.body).foregroundColor(.secondary).padding().frame(maxWidth: .infinity, alignment: .leading).background(Color.white).cornerRadius(12).shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                        }
                    }
                    Spacer(minLength: 40)
                    Button(action: onSwapRequest) {
                        HStack { Image(systemName: "arrow.left.arrow.right"); Text("Swap this Meal") }
                        .fontWeight(.bold).frame(maxWidth: .infinity).frame(height: 56).background(Color.nutriGreen.opacity(0.1)).foregroundColor(.nutriGreen).cornerRadius(16)
                    }
                }.padding()
            }
            .navigationTitle("Meal Details").navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .navigationBarTrailing) { Button("Close") { dismiss() } } }
            .background(Color.backgroundLight.ignoresSafeArea())
        }
    }
}
