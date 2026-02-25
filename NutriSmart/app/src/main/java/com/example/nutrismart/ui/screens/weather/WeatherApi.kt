package com.example.nutrismart.ui.screens.weather

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val main: MainData,
    val weather: List<WeatherData>,
    val name: String
)

data class MainData(
    val temp: Double,
    val humidity: Int
)

data class WeatherData(
    val main: String,
    val description: String
)

interface OpenWeatherApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = "56d5dbd4e55fd1262659601af9403e71"
    ): Response<WeatherResponse>
}

object WeatherRetrofit {
    val api: OpenWeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherApi::class.java)
    }
}