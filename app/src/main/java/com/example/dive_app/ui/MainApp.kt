// app/src/main/java/com/example/dive_app/ui/MainApp.kt
package com.example.dive_app.ui

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.example.dive_app.ui.theme.DiveTheme
import com.example.dive_app.ui.nav.AppNavHost
import com.example.dive_app.domain.viewmodel.*    // HealthViewModel 등

@Composable
fun MainApp(
    healthViewModel: HealthViewModel,
    fishViewModel: FishingPointViewModel,
    weatherViewModel: WeatherViewModel,
    tideViewModel: TideViewModel,
    locationViewModel: LocationViewModel,
    airQualityViewModel: AirQualityViewModel
) {
    DiveTheme {
        Scaffold { // 바텀/탑바 쓰면 padding 전달해서 쓰면 됨
            AppNavHost(
                healthViewModel = healthViewModel,
                fishViewModel = fishViewModel,
                weatherViewModel = weatherViewModel,
                tideViewModel = tideViewModel,
                locationViewModel = locationViewModel,
                airQualityViewModel = airQualityViewModel
            )
        }
    }
}
