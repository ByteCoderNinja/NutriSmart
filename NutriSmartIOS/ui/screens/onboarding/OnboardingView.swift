//
//  OnboardingView.swift
//  NutriSmartIOS
//

import SwiftUI

struct OnboardingView: View {
    @State private var viewModel = OnboardingViewModel()
    var isEditMode: Bool = false
    var onBackClick: () -> Void = {}
    var onProfileComplete: () -> Void
    @State private var showDatePicker = false
    
    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                HStack {
                    if isEditMode { Button(action: onBackClick) { Image(systemName: "arrow.left").foregroundColor(.nutriGreen).font(.title2) } }
                    Spacer()
                }.padding(.top, 8)
                Spacer().frame(height: 32)
                Text(isEditMode ? "Edit Your Plan" : "Build Your Plan").font(NutriSmartTypography.headlineMedium).foregroundColor(.nutriGreen)
                Text(isEditMode ? "Update your details for AI" : "Tell AI about yourself").font(NutriSmartTypography.bodyMedium).foregroundColor(.gray)
                Spacer().frame(height: 32)
                VStack(spacing: 24) { personalDetailsSection; goalsLifestyleSection; preferencesHealthSection }
                Spacer().frame(height: 24)
                if let error = viewModel.errorMessage { Text(error).foregroundColor(.red).font(.system(size: 14, weight: .medium)).padding(.bottom, 8) }
                if viewModel.isLoading {
                    VStack(spacing: 16) {
                        ProgressView().scaleEffect(1.5).tint(.nutriGreen)
                        Text(viewModel.loadingMessage).font(NutriSmartTypography.titleMedium).foregroundColor(.nutriGreen).multilineTextAlignment(.center)
                    }.padding(.vertical, 16)
                } else {
                    Button(action: { viewModel.submitProfile() }) {
                        Text("Generate My AI Plan").font(NutriSmartTypography.labelLarge).fontWeight(.bold).frame(maxWidth: .infinity).frame(height: 56).background(Color.nutriGreen).foregroundColor(.white).cornerRadius(24)
                    }.padding(.bottom, 50)
                }
            }.padding(.horizontal, 24)
        }
        .background(Color.backgroundLight.ignoresSafeArea())
        .onAppear { if isEditMode { viewModel.loadUserData() } }
        .onChange(of: viewModel.isComplete) { _, complete in if complete { onProfileComplete() } }
        .sheet(isPresented: $showDatePicker) {
            VStack {
                DatePicker("Birth Date", selection: $viewModel.birthDate, displayedComponents: .date).datePickerStyle(.graphical).padding()
                Button("Done") { showDatePicker = false }.padding()
            }.presentationDetents([.medium])
        }
    }
    
    private var personalDetailsSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            SectionHeader(title: "Personal Details")
            HStack(spacing: 12) {
                ForEach(Gender.allCases, id: \.self) { g in
                    let isSelected = viewModel.gender == g
                    Button(action: { viewModel.gender = g }) {
                        Text(g.rawValue).font(.system(size: 16, weight: .semibold)).frame(maxWidth: .infinity).padding(.vertical, 16)
                            .background(isSelected ? Color.nutriGreen.opacity(0.15) : Color(hex: 0xF1F4F1)).foregroundColor(isSelected ? .nutriGreen : .primary)
                            .cornerRadius(16).overlay(RoundedRectangle(cornerRadius: 16).stroke(isSelected ? Color.nutriGreen : Color.clear, lineWidth: 1))
                    }
                }
            }
            Button(action: { showDatePicker = true }) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) { Text("Date of Birth").font(.system(size: 12)).foregroundColor(.gray); Text(formatDate(viewModel.birthDate)).font(.system(size: 16)).foregroundColor(.primary) }
                    Spacer(); Image(systemName: "calendar").foregroundColor(.gray)
                }.padding().background(Color(hex: 0xF1F4F1)).cornerRadius(16)
            }
            SelectionRow(title: "Unit System", options: ["Metric", "Imperial"], selectedIndex: Binding(get: { viewModel.isImperial ? 1 : 0 }, set: { viewModel.isImperial = $0 == 1 }), labelProvider: { $0 })
            HStack(spacing: 12) {
                CustomTextField(value: $viewModel.height, placeholder: viewModel.isImperial ? "Height (ft)" : "Height (cm)", keyboardType: .decimalPad)
                CustomTextField(value: $viewModel.weight, placeholder: viewModel.isImperial ? "Weight (lbs)" : "Weight (kg)", keyboardType: .decimalPad)
            }
        }
    }
    
    private var goalsLifestyleSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            SectionHeader(title: "Goals & Lifestyle")
            CustomTextField(value: $viewModel.targetWeight, placeholder: viewModel.isImperial ? "Target Weight (lbs)" : "Target Weight (kg)", keyboardType: .decimalPad)
            Menu { ForEach(ActivityLevel.allCases, id: \.self) { level in Button(level.label) { viewModel.activityLevel = level } }
            } label: {
                HStack {
                    VStack(alignment: .leading, spacing: 4) { Text("Activity Level").font(.system(size: 12)).foregroundColor(.gray); Text(viewModel.activityLevel.label).font(.system(size: 16)).foregroundColor(.primary) }
                    Spacer(); Image(systemName: "chevron.down").foregroundColor(.gray)
                }.padding().background(Color(hex: 0xF1F4F1)).cornerRadius(16)
            }
            VStack(alignment: .leading, spacing: 8) {
                Text("Currency").font(.system(size: 14, weight: .medium)).foregroundColor(.gray)
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(Currency.allCases, id: \.self) { c in
                            let isSelected = viewModel.currency == c
                            Button(action: { viewModel.currency = c }) {
                                Text(c.rawValue).font(.system(size: 14, weight: .semibold)).padding(.horizontal, 16).padding(.vertical, 10)
                                    .background(isSelected ? Color.nutriGreen.opacity(0.15) : Color(hex: 0xF1F4F1)).foregroundColor(isSelected ? .nutriGreen : .primary)
                                    .cornerRadius(12).overlay(RoundedRectangle(cornerRadius: 12).stroke(isSelected ? Color.nutriGreen : Color.clear, lineWidth: 1))
                            }
                        }
                    }
                }
            }
            CustomTextField(value: $viewModel.budget, placeholder: "Daily Food Budget (\(viewModel.currency.rawValue))", keyboardType: .decimalPad)
        }
    }
    
    private var preferencesHealthSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            SectionHeader(title: "Foods to Avoid"); Text("Search ingredients you dislike or are allergic to:").font(.system(size: 12)).foregroundColor(.gray)
            VStack(spacing: 12) {
                HStack {
                    TextField("Search food...", text: $viewModel.foodSearchQuery).onChange(of: viewModel.foodSearchQuery) { _, newValue in viewModel.onFoodSearchQueryChanged(newValue) }
                    if viewModel.isSearchingFoods { ProgressView().tint(.nutriGreen) }
                }.padding().background(Color(hex: 0xF1F4F1)).cornerRadius(16)
                if !viewModel.foodSuggestions.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack { ForEach(viewModel.foodSuggestions, id: \.self) { food in Button(action: { viewModel.toggleDislikedFood(food); viewModel.foodSearchQuery = "" }) { Text(food).padding(.horizontal, 12).padding(.vertical, 6).background(Color.nutriGreen.opacity(0.1)).cornerRadius(8) } } }
                    }
                }
                FlowLayout(spacing: 8) { ForEach(Array(viewModel.selectedDislikedFoods), id: \.self) { food in HStack(spacing: 4) { Text(food); Button(action: { viewModel.toggleDislikedFood(food) }) { Image(systemName: "xmark.circle.fill") } }.padding(.horizontal, 12).padding(.vertical, 6).background(Color.nutriGreen).foregroundColor(.white).cornerRadius(20) } }
            }
            Spacer().frame(height: 16); SectionHeader(title: "Preferences & Health")
            MultiSelectPicker(label: "Dietary Preferences", items: DietaryPreference.allCases, selectedItems: $viewModel.selectedDietaryPreferences, itemLabel: { $0.rawValue.replacingOccurrences(of: "_", with: " ").lowercased().capitalized })
            MultiSelectPicker(label: "Medical Conditions", items: MedicalCondition.allCases, selectedItems: $viewModel.selectedMedicalConditions, itemLabel: { $0.rawValue.replacingOccurrences(of: "_", with: " ").lowercased().capitalized })
        }
    }
    private func formatDate(_ date: Date) -> String { let formatter = DateFormatter(); formatter.dateFormat = "yyyy-MM-dd"; return formatter.string(from: date) }
}
