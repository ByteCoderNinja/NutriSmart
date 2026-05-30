//
//  HomeComponents.swift
//  NutriSmartIOS
//

import SwiftUI

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

struct MacroDetailItem: View {
    let label: String
    let value: String
    let unit: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(color)
            Text(unit)
                .font(.system(size: 10))
                .foregroundColor(.secondary)
            Text(label)
                .font(.system(size: 10, weight: .semibold))
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
    }
}
