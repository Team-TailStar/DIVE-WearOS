package com.example.myapplication.domain.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State

class WeatherViewModel : ViewModel() {
    private val _uiState = mutableStateOf( WeatherData(
        sky = "맑음",
        windspd = "5 m/s",
        temp = "26 °C",
        humidity = "60%",
        rain = "0 mm",
        winddir = "북동풍",
        waveHt = "0.5 m",
        waveDir = "동쪽",
        obs_wt = "24 °C"
    ))
    val uiState: State<WeatherData> = _uiState

    fun updateWeather(
        sky: String,
        windspd: String,
        temp: String,
        humidity: String,
        rain: String,
        winddir: String,
        waveHt: String,
        waveDir: String,
        obs_wt: String
    ) {
        _uiState.value = WeatherData(
            sky = sky,
            windspd = windspd,
            temp = temp,
            humidity = humidity,
            rain = rain,
            winddir = winddir,
            waveHt = waveHt,
            waveDir = waveDir,
            obs_wt = obs_wt
        )
    }
}