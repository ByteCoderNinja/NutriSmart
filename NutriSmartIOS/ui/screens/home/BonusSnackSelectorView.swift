//
//  BonusSnackSelectorView.swift
//  NutriSmartIOS
//

import SwiftUI

struct BonusSnackSelectorView: View {
    var viewModel: HomeViewModel
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationStack {
            VStack {
                if viewModel.uiState.isSwapping {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if viewModel.uiState.availableBonusSnacks.isEmpty {
                    ContentUnavailableView(
                        "No snacks available",
                        systemImage: "info.circle",
                        description: Text("Try burning more calories to unlock more options.")
                    )
                } else {
                    List(viewModel.uiState.availableBonusSnacks) { meal in
                        Button(action: {
                            viewModel.uiState.bonusSnack = meal
                            dismiss()
                        }) {
                            HStack {
                                VStack(alignment: .leading) {
                                    Text(meal.name).font(.headline)
                                    Text("\(meal.calories) kcal | P:\(meal.protein)g C:\(meal.carbs)g F:\(meal.fat)g")
                                        .font(.subheadline)
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                                Image(systemName: "plus.circle").foregroundColor(.nutriGreen)
                            }
                            .padding(.vertical, 8)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                    .listStyle(InsetGroupedListStyle())
                }
            }
            .navigationTitle("Choose Bonus Snack")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { dismiss() }
                }
            }
            .background(Color.backgroundLight.ignoresSafeArea())
        }
    }
}
