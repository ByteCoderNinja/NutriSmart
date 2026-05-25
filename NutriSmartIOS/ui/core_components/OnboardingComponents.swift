//
//  OnboardingComponents.swift
//  NutriSmartIOS
//

import SwiftUI

struct SectionHeader: View {
    let title: String
    
    var body: some View {
        Text(title)
            .font(NutriSmartTypography.titleMedium)
            .foregroundColor(.nutriGreen)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, 16)
            .padding(.bottom, 8)
    }
}

struct SelectionRow<T: Hashable>: View {
    let title: String
    let options: [T]
    @Binding var selectedIndex: Int
    let labelProvider: (T) -> String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.gray)
            
            HStack(spacing: 8) {
                ForEach(0..<options.count, id: \.self) { index in
                    let isSelected = selectedIndex == index
                    Button(action: { selectedIndex = index }) {
                        Text(labelProvider(options[index]))
                            .font(.system(size: 14, weight: .semibold))
                            .padding(.vertical, 12)
                            .frame(maxWidth: .infinity)
                            .background(isSelected ? Color.nutriGreen.opacity(0.15) : Color(hex: "F1F4F1"))
                            .foregroundColor(isSelected ? .nutriGreen : .primary)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(isSelected ? Color.nutriGreen : Color.clear, lineWidth: 1)
                            )
                    }
                }
            }
        }
    }
}

struct MultiSelectPicker<T: Hashable & CaseIterable>: View where T.AllCases: RandomAccessCollection {
    let label: String
    let items: T.AllCases
    @Binding var selectedItems: [T]
    let itemLabel: (T) -> String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(label)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.gray)
            
            FlowLayout(spacing: 8) {
                ForEach(items, id: \.self) { item in
                    let isSelected = selectedItems.contains(item)
                    Button(action: {
                        if isSelected {
                            selectedItems.removeAll { $0 == item }
                        } else {
                            selectedItems.append(item)
                        }
                    }) {
                        Text(itemLabel(item))
                            .font(.system(size: 14))
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(isSelected ? Color.nutriGreen : Color(hex: "F1F4F1"))
                            .foregroundColor(isSelected ? .white : .primary)
                            .cornerRadius(20)
                    }
                }
            }
        }
    }
}

// Simple FlowLayout for multi-select chips
struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let width = proposal.width ?? .infinity
        var height: CGFloat = 0
        var x: CGFloat = 0
        var y: CGFloat = 0
        var maxHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > width {
                x = 0
                y += maxHeight + spacing
                maxHeight = 0
            }
            x += size.width + spacing
            maxHeight = max(maxHeight, size.height)
        }
        height = y + maxHeight
        return CGSize(width: width, height: height)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX
        var y = bounds.minY
        var maxHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX {
                x = bounds.minX
                y += maxHeight + spacing
                maxHeight = 0
            }
            subview.place(at: CGPoint(x: x, y: y), proposal: .unspecified)
            x += size.width + spacing
            maxHeight = max(maxHeight, size.height)
        }
    }
}
