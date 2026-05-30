//
//  Theme.swift
//  NutriSmartIOS
//
//  Created by Alex on 29/05/2026.
//

import SwiftUI

extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xff) / 255,
            green: Double((hex >> 08) & 0xff) / 255,
            blue: Double((hex >> 00) & 0xff) / 255,
            opacity: alpha
        )
    }

    static let nutriGreen = Color(hex: 0x5DB056)
    static let nutriBlue = Color(hex: 0x2881B4)
    static let nutriOrange = Color(hex: 0xF58A2E)

    static let backgroundLight = Color(hex: 0xF9FBF9)
    static let surfaceLight = Color(hex: 0xFFFFFF)
    static let textPrimary = Color(hex: 0x2D3142)
    static let textSecondary = Color(hex: 0x9098B1)
    
    static let purple80 = Color(hex: 0xD0BCFF)
    static let purpleGrey80 = Color(hex: 0xCCC2DC)
    static let pink80 = Color(hex: 0xEFB8C8)
    static let purple40 = Color(hex: 0x6650a4)
    static let purpleGrey40 = Color(hex: 0x625b71)
    static let pink40 = Color(hex: 0x7D5260)
}
