package com.example.myapplication.domain.model

// 단일 날씨 데이터 모델
data class WeatherData(
    val region: String,
    val temp: String,
    val condition: String,
    val wind: String,
    val humidity: String,
    val wave: String,
    val notice: String
)