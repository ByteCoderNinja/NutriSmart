package com.example.nutrismart.ui.screens.weather

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeatherUiState(
    val isLoading: Boolean = true,
    val temperature: Int = 0,
    val condition: String = "",
    val humidity: Int = 0,
    val cityName: String = "Locating...",
    val recommendations: List<String> = emptyList()
)

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun fetchRealWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = WeatherRetrofit.api.getCurrentWeather(lat, lon)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val currentTemp = body.main.temp.toInt()
                        val currentCondition = body.weather.firstOrNull()?.main ?: "Unknown"
                        val currentHumidity = body.main.humidity

                        val recs = generateRecommendations(currentTemp, currentCondition)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                temperature = currentTemp,
                                condition = currentCondition,
                                humidity = currentHumidity,
                                cityName = body.name,
                                recommendations = recs
                            )
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, condition = "API Error", cityName = "Error") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, condition = "No Connection", cityName = "Offline") }
            }
        }
    }

    private fun generateRecommendations(temp: Int, condition: String): List<String> {
        val recs = mutableListOf<String>()

        if (temp >= 30) {
            recs.add("It's a heatwave! Make sure to increase your daily water intake.")
            recs.add("Avoid direct sun exposure between 11:00 AM and 4:00 PM.")
        }

        if (condition.contains("Rain", ignoreCase = true) || condition.contains("Drizzle", ignoreCase = true)) {
            recs.add("It's raining outside. Consider replacing your outdoor run with an indoor workout.")
        } else if (temp in 15..25 && !condition.contains("Rain", ignoreCase = true)) {
            recs.add("Perfect weather for a walk or an outdoor run!")
        }

        return recs
    }

    fun checkHydrationAndNotify(context: Context, waterConsumedMl: Int) {
        val state = _uiState.value
        if (state.temperature >= 30 && waterConsumedMl < 1000) {
            val intent = Intent(context, WeatherNotificationReceiver::class.java)
            context.sendBroadcast(intent)
        }
    }
}