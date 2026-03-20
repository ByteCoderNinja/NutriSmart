package com.example.nutrismart.data.repository

import com.example.nutrismart.data.remote.WeatherResponse
import com.example.nutrismart.data.remote.WeatherRetrofit
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor() {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Response<WeatherResponse> {
        return WeatherRetrofit.api.getCurrentWeather(lat, lon)
    }
}
