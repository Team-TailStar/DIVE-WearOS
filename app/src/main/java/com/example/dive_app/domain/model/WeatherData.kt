package com.example.dive_app.domain.model

data class WeatherData(
    val sky: String = "",
    val windspd: String = "",
    val temp: String = "",
    val humidity: String = "",
    val rain: String = "",
    val winddir: String = "",
    val waveHt: String = "",
    val waveDir: String = "",
    val obs_wt: String = ""
)