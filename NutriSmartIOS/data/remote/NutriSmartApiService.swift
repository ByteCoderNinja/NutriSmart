import Foundation

class NutriSmartApiService {
    static let shared = NutriSmartApiService()
    private init() {}

    func register(request: RegisterRequest) async throws -> EmptyResponse {
        return try await NetworkManager.shared.request(
            endpoint: "auth/register",
            method: "POST",
            body: request
        )
    }
    
    func login(request: AuthRequest) async throws -> AuthResponse {
        return try await NetworkManager.shared.request(
            endpoint: "auth/login",
            method: "POST",
            body: request
        )
    }
    
    func verify(request: VerifyRequest) async throws -> AuthResponse {
        return try await NetworkManager.shared.request(
            endpoint: "auth/verify",
            method: "POST",
            body: request
        )
    }
    
    func resendCode(email: String) async throws -> EmptyResponse {
        return try await NetworkManager.shared.request(
            endpoint: "auth/resend-code?email=\(email)",
            method: "POST"
        )
    }
    
    func googleLogin(request: GoogleLoginRequest) async throws -> AuthResponse {
        return try await NetworkManager.shared.request(
            endpoint: "auth/google",
            method: "POST",
            body: request
        )
    }

    func forgotPassword(request: [String: String]) async throws -> EmptyResponse {
        return try await NetworkManager.shared.request(
            endpoint: "auth/forgot-password",
            method: "POST",
            body: request
        )
    }

    func resetPassword(request: [String: String]) async throws -> EmptyResponse {
        return try await NetworkManager.shared.request(
            endpoint: "auth/reset-password",
            method: "POST",
            body: request
        )
    }

    func completeProfile(token: String, request: OnboardingRequest) async throws -> EmptyResponse {
        return try await NetworkManager.shared.request(
            endpoint: "users/onboarding",
            method: "POST",
            body: request,
            token: token
        )
    }
    
    func getUser(token: String, userId: Int) async throws -> UserDto {
        return try await NetworkManager.shared.request(
            endpoint: "users/\(userId)",
            method: "GET",
            token: token
        )
    }

    func patchUser(token: String, userId: Int, request: UpdateUserRequest) async throws -> UserDto {
        return try await NetworkManager.shared.request(
            endpoint: "users/\(userId)",
            method: "PATCH",
            body: request,
            token: token
        )
    }

    func deleteUser(token: String, userId: Int) async throws -> EmptyResponse {
        return try await NetworkManager.shared.request(
            endpoint: "users/\(userId)",
            method: "DELETE",
            token: token
        )
    }

    func startPlanGeneration(token: String, userId: Int) async throws -> [String: String] {
        return try await NetworkManager.shared.request(
            endpoint: "nutrition/generate/\(userId)",
            method: "POST",
            token: token
        )
    }

    func checkGenerationStatus(token: String, userId: Int) async throws -> [String: String] {
        return try await NetworkManager.shared.request(
            endpoint: "nutrition/status/\(userId)",
            method: "GET",
            token: token
        )
    }

    func getTodayPlan(token: String, userId: Int) async throws -> MealPlanDto {
        return try await NetworkManager.shared.request(
            endpoint: "nutrition/today/\(userId)",
            method: "GET",
            token: token
        )
    }
    
    func getDailyPlan(token: String, userId: Int, date: String) async throws -> MealPlanDto {
        return try await NetworkManager.shared.request(
            endpoint: "nutrition/plan?userId=\(userId)&date=\(date)",
            method: "GET",
            token: token
        )
    }

    func toggleMealConsumed(token: String, mealId: Int, consumed: Bool) async throws -> MealDto {
        return try await NetworkManager.shared.request(
            endpoint: "nutrition/meal/\(mealId)/consume?consumed=\(consumed)",
            method: "PATCH",
            token: token
        )
    }

    func getMealAlternatives(token: String, userId: Int, mealType: String) async throws -> [MealDto] {
        return try await NetworkManager.shared.request(
            endpoint: "nutrition/alternatives/\(userId)?type=\(mealType)",
            method: "GET",
            token: token
        )
    }

    func swapMeal(token: String, userId: Int, mealType: String, newMealId: Int, date: String) async throws -> MealPlanDto {
        return try await NetworkManager.shared.request(
            endpoint: "nutrition/swap/\(userId)?mealType=\(mealType)&newMealId=\(newMealId)&date=\(date)",
            method: "POST",
            token: token
        )
    }

    func getShoppingList(token: String, userId: Int) async throws -> ShoppingListDto {
        return try await NetworkManager.shared.request(
            endpoint: "nutrition/shopping-list/\(userId)",
            method: "GET",
            token: token
        )
    }

    func toggleShoppingItem(token: String, itemId: Int, isChecked: Bool) async throws -> EmptyResponse {
        return try await NetworkManager.shared.request(
            endpoint: "nutrition/shopping-item/\(itemId)/check?checked=\(isChecked)",
            method: "PATCH",
            token: token
        )
    }

    func searchFoods(token: String, query: String) async throws -> [String] {
        return try await NetworkManager.shared.request(
            endpoint: "foods/search?query=\(query)",
            method: "GET",
            token: token
        )
    }
}

class WeatherService {
    static let shared = WeatherService()
    private init() {}
    
    private let apiKey = "01f8332dadd916b12dc66396063092c2"
    private let baseURL = "https://api.openweathermap.org/data/2.5/"
    
    func getCurrentWeather(lat: Double, lon: Double) async throws -> WeatherResponse {
        let urlString = "\(baseURL)weather?lat=\(lat)&lon=\(lon)&units=metric&appid=\(apiKey)"
        guard let url = URL(string: urlString) else {
            throw NetworkError.invalidURL
        }
        
        let (data, response) = try await URLSession.shared.data(for: URLRequest(url: url))
        
        guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
            throw NetworkError.serverError("Weather API error: \((response as? HTTPURLResponse)?.statusCode ?? 0)")
        }
        
        return try JSONDecoder().decode(WeatherResponse.self, from: data)
    }
}
