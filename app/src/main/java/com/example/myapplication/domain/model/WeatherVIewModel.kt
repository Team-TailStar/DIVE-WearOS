package com.example.myapplication.domain.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State

class WeatherViewModel : ViewModel() {
    private val _uiState = mutableStateOf(WeatherData())
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