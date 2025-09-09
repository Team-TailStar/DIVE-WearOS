package com.example.dive_app.domain.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import com.example.dive_app.domain.model.WeatherData

class WeatherViewModel : ViewModel() {
    private val _uiState = mutableStateOf(WeatherData())
    val uiState: State<WeatherData> = _uiState

    fun updateWeather(
        weatherData: WeatherData
    ) {
        val newData = WeatherData(
            sky = weatherData.sky,
            windspd = weatherData.windspd,
            temp = weatherData.temp,
            humidity = weatherData.humidity,
            rain = weatherData.rain,
            winddir = weatherData.winddir,
            waveHt = weatherData.waveHt,
            waveDir = weatherData.waveDir,
            obsWt = weatherData.obsWt
        )

        // 📌 로그 출력 (값 확인)
        Log.d("WeatherViewModel", "✅ updateWeather 호출됨: $newData")
        _uiState.value = newData
    }
}
