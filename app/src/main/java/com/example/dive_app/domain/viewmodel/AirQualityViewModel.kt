package com.example.dive_app.domain.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import com.example.dive_app.domain.model.AirQuality

class AirQualityViewModel : ViewModel() {
    private val _uiState = mutableStateOf(AirQuality())
    val uiState: State<AirQuality> = _uiState

    fun updateAirQuality(
        airQuality: AirQuality
    ) {
        val newData = AirQuality(
            no2Value = airQuality.no2Value,
            o3Value = airQuality.o3Value,
            pm10Value = airQuality.pm10Value,
            pm25Value = airQuality.pm25Value,
            o3Grade = airQuality.o3Grade,
            no2Grade = airQuality.no2Grade,
            pm10Grade = airQuality.pm10Grade,
            pm25Grade = airQuality.pm25Grade
        )

        // 📌 로그 출력 (값 확인)
        Log.d("WatchMsg", "✅ updateAirQuality 호출됨: $newData")
        _uiState.value = newData
    }
}
