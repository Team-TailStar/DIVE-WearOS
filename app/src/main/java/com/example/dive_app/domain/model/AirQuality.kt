package com.example.dive_app.domain.model

data class AirQuality(
    val no2Value : Double = 0.0,
    val o3Value : Double = 0.0,
    val pm10Value : Double = 0.0,
    val pm25Value : Double = 0.0,
    val o3Grade : Int = 0,
    val no2Grade : Int = 0,
    val pm10Grade : Int = 0,
    val pm25Grade : Int = 0,
)
