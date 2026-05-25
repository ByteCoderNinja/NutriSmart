//
//  WeatherView.swift
//  NutriSmartIOS
//

import SwiftUI
import CoreLocation

struct WeatherView: View {
    @State private var viewModel = WeatherViewModel()
    @State private var locationManager = LocationManager()
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            if viewModel.uiState.isLoading { Spacer(); HStack { Spacer(); VStack { ProgressView(); Text("Fetching weather...").foregroundColor(.secondary).padding(.top, 8) }; Spacer() }; Spacer() }
            else {
                Text("Weather & Tips").font(NutriSmartTypography.headlineMedium).foregroundColor(.nutriGreen).padding(.top, 16)
                HStack(spacing: 4) { Image(systemName: "location.fill").font(.caption); Text(viewModel.uiState.cityName).font(.system(size: 16)) }.foregroundColor(.secondary).padding(.bottom, 8)
                VStack(spacing: 24) {
                    Image(systemName: weatherIcon).font(.system(size: 64)).foregroundColor(weatherIconColor)
                    VStack(spacing: 4) { Text("\(viewModel.uiState.temperature)°C").font(.system(size: 48, weight: .black)); Text(viewModel.uiState.condition).font(.system(size: 20)).opacity(0.8) }.foregroundColor(.white)
                    Divider().background(Color.white.opacity(0.3))
                    HStack { WeatherDetailItem(label: "Humidity", value: "\(viewModel.uiState.humidity)%"); Spacer(); WeatherDetailItem(label: "Condition", value: viewModel.uiState.condition) }.padding(.horizontal, 24)
                }.padding(24).frame(maxWidth: .infinity).background(Color.nutriBlue).cornerRadius(24).shadow(radius: 5)
                Text("Today's Recommendations").font(.system(size: 20, weight: .bold)).padding(.top, 16)
                ScrollView { VStack(spacing: 12) { ForEach(viewModel.uiState.recommendations, id: \.self) { rec in RecommendationCard(text: rec) } } }
            }
        }.padding(.horizontal, 16).background(Color.backgroundLight.ignoresSafeArea())
        .onAppear { locationManager.requestLocation() }
        .onChange(of: locationManager.location) { _, newLocation in if let loc = newLocation { viewModel.fetchRealWeather(lat: loc.coordinate.latitude, lon: loc.coordinate.longitude) } }
    }
    private var weatherIcon: String { let c = viewModel.uiState.condition.lowercased(); return c.contains("rain") ? "cloud.rain.fill" : (c.contains("cloud") ? "cloud.fill" : "sun.max.fill") }
    private var weatherIconColor: Color { return viewModel.uiState.condition.lowercased().contains("rain") ? .white : Color(hex: "FFD54F") }
}

struct WeatherDetailItem: View {
    let label: String; let value: String
    var body: some View { VStack { Text(label).font(.system(size: 14)).opacity(0.7); Text(value).font(.system(size: 18, weight: .bold)) }.foregroundColor(.white) }
}

struct RecommendationCard: View {
    let text: String
    var body: some View { HStack(alignment: .top, spacing: 12) { Image(systemName: "info.circle.fill").foregroundColor(.nutriGreen).font(.title3); Text(text).font(.system(size: 15)).lineSpacing(4); Spacer() }.padding(16).background(Color.white).cornerRadius(16).shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2) }
}

@Observable
class LocationManager: NSObject, CLLocationManagerDelegate {
    private let manager = CLLocationManager(); var location: CLLocation?
    override init() { super.init(); manager.delegate = self }
    func requestLocation() { manager.requestWhenInUseAuthorization(); manager.startUpdatingLocation() }
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) { location = locations.last; manager.stopUpdatingLocation() }
}
