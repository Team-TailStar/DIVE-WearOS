package com.example.dive_app

import WeatherScreen
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.common.theme.MyApplicationTheme
import com.example.myapplication.domain.model.FishingPoint
import com.example.myapplication.domain.model.HealthViewModel
import com.example.myapplication.domain.model.TideInfoData
import com.example.myapplication.domain.model.TideViewModel
import com.example.myapplication.ui.screen.AirQualityScreen
import com.example.myapplication.ui.screen.FishingDetailPage
import com.example.myapplication.ui.screen.HealthScreen
import com.example.myapplication.ui.screen.HomeScreen
import com.example.myapplication.ui.screen.LocationScreen
import com.example.myapplication.ui.screen.TideWatchScreen
import com.example.myapplication.ui.screen.SeaWeatherScreen
import com.example.myapplication.ui.screen.TideDetailPage
import com.example.myapplication.ui.screen.WeatherMenuScreen
import com.google.gson.Gson
import androidx.compose.runtime.getValue

@Composable
fun MainApp(vm: HealthViewModel) {
    MyApplicationTheme {
        val bpm by vm.currentBpm.collectAsState()
        val navController = rememberNavController()
        val gson = Gson()

        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(navController)
            }
            composable("location") {
                LocationScreen(navController)
            }
            composable("fishingDetail") { backStackEntry ->
                val point = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<FishingPoint>("fishingPoint")
                if (point != null) {
                    FishingDetailPage(point)
                }
            }
            composable("tide") {
                TideWatchScreen(navController = navController)
            }

            composable("tideDetail") { backStackEntry ->
                val tide = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<TideInfoData>("selectedTide")

                if (tide != null) {
                    TideDetailPage(tide, navController)
                }
            }

            composable("weather") {
                WeatherScreen(navController)
            }
            composable("weatherMenu") { WeatherMenuScreen(navController) }
            composable("sea_weather") { SeaWeatherScreen(navController) }
            composable("air_quality") { AirQualityScreen() }
            composable("health") { HealthScreen(vm) }

        }
    }
}
