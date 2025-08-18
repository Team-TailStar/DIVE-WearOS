package com.example.dive_app

import WeatherScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dive_app.common.theme.MyApplicationTheme
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.model.HealthViewModel
import com.example.dive_app.domain.model.TideInfoData
import com.example.dive_app.ui.screen.AirQualityScreen
import com.example.dive_app.ui.screen.FishingDetailPage
import com.example.dive_app.ui.screen.HealthScreen
import com.example.dive_app.ui.screen.HomeScreen
import com.example.dive_app.ui.screen.LocationScreen
import com.example.dive_app.ui.screen.TideWatchScreen
import com.example.dive_app.ui.screen.SeaWeatherScreen
import com.example.dive_app.ui.screen.TideDetailPage
import com.example.dive_app.ui.screen.WeatherMenuScreen
import com.google.gson.Gson
import androidx.compose.runtime.getValue
import com.example.dive_app.domain.model.TideViewModel
import com.example.dive_app.domain.model.WeatherViewModel
import com.example.dive_app.ui.viewmodel.FishingPointViewModel

@Composable
fun MainApp(
    healthVM: HealthViewModel,
    fishingVM: FishingPointViewModel,
    weatherVM: WeatherViewModel,
    tideVM: TideViewModel,
    repo: com.example.dive_app.data.repository.WearDataRepository
) {
    MyApplicationTheme {
        val bpm by healthVM.currentBpm.collectAsState()
        val navController = rememberNavController()
        val gson = Gson()

        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(navController, repo)
            }
            composable("location") {
                LocationScreen(navController)
            }
            composable("fishingDetail") {
                val point = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<FishingPoint>("fishingPoint")
                if (point != null) {
                    FishingDetailPage(point)
                }
            }
            composable("tide") {
                TideWatchScreen(navController = navController, tideVM = tideVM, repo = repo)
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
                WeatherScreen(navController = navController, weatherVM, repo)
            }
            composable("weatherMenu") { WeatherMenuScreen(navController) }
            composable("sea_weather") { SeaWeatherScreen(navController, weatherVM) }
            composable("air_quality") { AirQualityScreen() }
            composable("health") { HealthScreen(healthVM) }
        }
    }
}