//
//  MealSwapView.swift
//  NutriSmartIOS
//

import SwiftUI

struct MealSwapView: View {
    var viewModel: HomeViewModel
    let mealType: String
    var onSwapSuccess: () -> Void
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationStack {
            VStack {
                if viewModel.uiState.isSwapping {
                    ProgressView("Finding AI alternatives...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List(viewModel.uiState.alternatives) { meal in
                        Button(action: {
                            viewModel.swapMeal(type: mealType, newMealId: meal.id)
                            dismiss()
                            onSwapSuccess()
                        }) {
                            HStack {
                                VStack(alignment: .leading) {
                                    Text(meal.name).font(.headline)
                                    Text("\(meal.calories) kcal | P:\(meal.protein)g C:\(meal.carbs)g F:\(meal.fat)g")
                                        .font(.subheadline)
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                                Image(systemName: "plus.circle.fill").foregroundColor(.nutriGreen)
                            }
                            .padding(.vertical, 8)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                    .listStyle(InsetGroupedListStyle())
                }
            }
            .navigationTitle("Swap \(mealType.capitalized)")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { dismiss() }
                }
            }
            .background(Color.backgroundLight.ignoresSafeArea())
            .onAppear {
                viewModel.loadAlternatives(type: mealType)
            }
        }
    }
}
