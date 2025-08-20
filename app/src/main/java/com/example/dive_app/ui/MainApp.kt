package com.example.dive_app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import com.example.dive_app.common.theme.MyApplicationTheme
import com.example.dive_app.ui.nav.AppNavHost

// ViewModels
import com.example.dive_app.domain.viewmodel.AirQualityViewModel
import com.example.dive_app.domain.viewmodel.HealthViewModel
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.domain.viewmodel.TideViewModel
import com.example.dive_app.domain.viewmodel.WeatherViewModel
import com.example.dive_app.ui.viewmodel.FishingPointViewModel

@Composable
fun MainApp(
    healthViewModel: HealthViewModel,
    fishViewModel: FishingPointViewModel,
    weatherViewModel: WeatherViewModel,
    tideViewModel: TideViewModel,
    locationViewModel: LocationViewModel,
    airQualityViewModel: AirQualityViewModel
) {
    MyApplicationTheme {
        Scaffold { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
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
}
