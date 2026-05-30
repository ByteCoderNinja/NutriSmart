//
//  ProfileComponents.swift
//  NutriSmartIOS
//

import SwiftUI

struct MiniStatCard: View {
    let icon: String
    let value: String
    let unit: String
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon).foregroundColor(.nutriGreen)
            Text(value).font(.system(size: 18, weight: .bold))
            Text(unit).font(.system(size: 12)).foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity).padding(.vertical, 16).background(Color.white).cornerRadius(16).shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
    }
}
