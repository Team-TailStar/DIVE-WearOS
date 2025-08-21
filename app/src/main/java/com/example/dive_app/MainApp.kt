package com.example.dive_app

import WeatherScreen
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dive_app.common.theme.MyApplicationTheme
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.viewmodel.HealthViewModel
import com.example.dive_app.domain.model.TideInfoData
import com.example.dive_app.domain.viewmodel.AirQualityViewModel
import com.example.dive_app.domain.viewmodel.EmergencyEvent
import com.example.dive_app.ui.screen.weather.AirQualityScreen
import com.example.dive_app.ui.screen.location.FishingDetailScreen
import com.example.dive_app.ui.screen.health.HealthScreen
import com.example.dive_app.ui.screen.HomeScreen
import com.example.dive_app.ui.screen.location.LocationScreen
import com.example.dive_app.ui.screen.tide.TideWatchScreen
import com.example.dive_app.ui.screen.weather.SeaWeatherScreen
import com.example.dive_app.ui.screen.tide.TideDetailPage
import com.example.dive_app.ui.screen.weather.WeatherMenuScreen
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.domain.viewmodel.TideViewModel
import com.example.dive_app.domain.viewmodel.WeatherViewModel
import com.example.dive_app.ui.screen.alert.EmergencyScreen
import com.example.dive_app.ui.viewmodel.FishingPointViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun MainApp(
    healthVM: HealthViewModel,
    fishingVM: FishingPointViewModel,
    weatherVM: WeatherViewModel,
    tideVM: TideViewModel,
    locationVM: LocationViewModel,
    airQualityVM: AirQualityViewModel
) {
    MyApplicationTheme {
        val navController = rememberNavController()

        LaunchedEffect(Unit) {
            healthVM.emergencyEvent
                .distinctUntilChanged()
                .collect { event ->
                    when (event) {
                        is EmergencyEvent.HeartRateLow,
                        is EmergencyEvent.Spo2Low,
                        is EmergencyEvent.ScreenTapped -> {
                            if (navController.currentBackStackEntry?.destination?.route != "emergency") {
                                navController.navigate("emergency")
                            }
                        }
                    }
                }
        }

        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(navController)
            }
            composable("location") {
                LocationScreen(navController, fishingVM, locationVM)
            }
            composable("fishingDetail") {
                val point = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<FishingPoint>("fishingPoint")
                if (point != null) {
                    FishingDetailScreen(point)
                }
            }
            composable("tide") {
                TideWatchScreen(navController = navController, tideVM = tideVM)
            }
            composable("tideDetail") {
                val tide = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<TideInfoData>("selectedTide")

                if (tide != null) {
                    TideDetailPage(tide, navController)
                }
            }
            composable("emergency") {
                EmergencyScreen(navController)
            }
            composable("weather") {
                WeatherScreen(navController = navController, weatherVM)
            }
            composable("weatherMenu") { WeatherMenuScreen(navController) }
            composable("sea_weather") { SeaWeatherScreen(navController, weatherVM) }
            composable("air_quality") { AirQualityScreen(navController, airQualityVM) }
            composable("health") { HealthScreen(healthVM) }
        }
    }
}