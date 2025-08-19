package com.example.dive_app

import WeatherScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dive_app.common.theme.MyApplicationTheme
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.viewmodel.HealthViewModel
import com.example.dive_app.domain.model.TideInfoData
import com.example.dive_app.domain.viewmodel.AirQualityViewModel
import com.example.dive_app.ui.screen.AirQualityScreen
import com.example.dive_app.ui.screen.FishingDetailScreen
import com.example.dive_app.ui.screen.HealthScreen
import com.example.dive_app.ui.screen.HomeScreen
import com.example.dive_app.ui.screen.LocationScreen
import com.example.dive_app.ui.screen.TideWatchScreen
import com.example.dive_app.ui.screen.SeaWeatherScreen
import com.example.dive_app.ui.screen.TideDetailPage
import com.example.dive_app.ui.screen.WeatherMenuScreen
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.domain.viewmodel.TideViewModel
import com.example.dive_app.domain.viewmodel.WeatherViewModel
import com.example.dive_app.ui.viewmodel.FishingPointViewModel

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