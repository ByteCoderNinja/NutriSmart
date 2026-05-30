//
//  ShoppingListView.swift
//  NutriSmartIOS
//

import SwiftUI

struct ShoppingListView: View {
    var viewModel: HomeViewModel
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationStack {
            VStack {
                if let list = viewModel.uiState.shoppingList {
                    if list.items.isEmpty {
                        ContentUnavailableView("No Items", systemImage: "cart", description: Text("Your shopping list is empty."))
                    } else {
                        List {
                            ForEach(viewModel.itemsByCategory.keys.sorted(), id: \.self) { category in
                                Section(header: Text(category).font(.headline).foregroundColor(.nutriGreen)) {
                                    ForEach(viewModel.itemsByCategory[category] ?? []) { item in
                                        Button(action: {
                                            viewModel.toggleShoppingListItem(itemId: item.id, checked: !item.checked)
                                        }) {
                                            HStack {
                                                Image(systemName: item.checked ? "checkmark.circle.fill" : "circle")
                                                    .foregroundColor(item.checked ? .nutriGreen : .gray)
                                                Text(item.name)
                                                    .strikethrough(item.checked)
                                                    .foregroundColor(item.checked ? .secondary : .primary)
                                                Spacer()
                                            }
                                        }
                                        .buttonStyle(PlainButtonStyle())
                                    }
                                }
                            }
                        }
                        .listStyle(InsetGroupedListStyle())
                    }
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("Shopping List")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
            .background(Color.backgroundLight.ignoresSafeArea())
        }
    }
}

extension HomeViewModel {
    var itemsByCategory: [String: [ShoppingListItemDto]] {
        guard let items = uiState.shoppingList?.items else { return [:] }
        return Dictionary(grouping: items, by: { $0.category })
    }
}
