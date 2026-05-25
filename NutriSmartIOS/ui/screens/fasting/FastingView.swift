//
//  FastingView.swift
//  NutriSmartIOS
//

import SwiftUI

struct FastingView: View {
    @State private var viewModel = FastingViewModel()
    
    var body: some View {
        VStack(spacing: 0) {
            Text("Intermittent Fasting")
                .font(NutriSmartTypography.headlineMedium)
                .foregroundColor(.nutriGreen)
                .padding(.top, 24)
                .padding(.bottom, 32)
            
            // Progress Circle
            ZStack {
                Circle()
                    .stroke(Color.gray.opacity(0.15), lineWidth: 12)
                    .frame(width: 280, height: 280)
                
                Circle()
                    .trim(from: 0, to: CGFloat(viewModel.progress))
                    .stroke(Color.nutriGreen, style: StrokeStyle(lineWidth: 12, lineCap: .round))
                    .frame(width: 280, height: 280)
                    .rotationEffect(.degrees(-90))
                    .animation(.linear, value: viewModel.progress)
                
                VStack(spacing: 8) {
                    if viewModel.isFasting {
                        Text("Time Remaining")
                            .font(.system(size: 16))
                            .foregroundColor(.secondary)
                        Text(viewModel.timeRemainingString)
                            .font(.system(size: 40, weight: .black))
                            .monospacedDigit()
                    } else {
                        Text("Ready to start?")
                            .font(.system(size: 18))
                            .foregroundColor(.secondary)
                        Text("\(viewModel.selectedDurationHours) Hours")
                            .font(.system(size: 32, weight: .bold))
                    }
                }
            }
            .padding(16)
            
            Spacer().frame(height: 40)
            
            Text("Select Duration")
                .font(.system(size: 16, weight: .semibold))
                .padding(.bottom, 8)
            
            HStack(spacing: 12) {
                let options = [12, 14, 16, 18, 24]
                ForEach(options, id: \.self) { hours in
                    let isSelected = viewModel.selectedDurationHours == hours
                    Button(action: { viewModel.selectDuration(hours) }) {
                        Text("\(hours)h")
                            .font(.system(size: 14, weight: .medium))
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(isSelected ? Color.nutriGreen : Color.gray.opacity(0.1))
                            .foregroundColor(isSelected ? .white : .primary)
                            .cornerRadius(20)
                    }
                    .disabled(viewModel.isFasting)
                }
            }
            
            Spacer()
            
            Button(action: { viewModel.toggleFasting() }) {
                HStack(spacing: 8) {
                    Image(systemName: viewModel.isFasting ? "stop.fill" : "flame.fill")
                        .font(.title3)
                    Text(viewModel.isFasting ? "End Fast" : "Start Fasting")
                        .font(.system(size: 18, weight: .bold))
                }
                .frame(maxWidth: .infinity)
                .frame(height: 60)
                .background(viewModel.isFasting ? Color.orange : Color.nutriGreen)
                .foregroundColor(.white)
                .cornerRadius(16)
            }
            .padding(.bottom, 32)
        }
        .padding(.horizontal, 16)
        .background(Color.backgroundLight.ignoresSafeArea())
    }
}
