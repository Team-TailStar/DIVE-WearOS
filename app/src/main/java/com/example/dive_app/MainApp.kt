package com.example.dive_app

import com.example.dive_app.ui.screen.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dive_app.common.theme.MyApplicationTheme
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.model.TideInfoData
import com.example.dive_app.domain.viewmodel.*
import com.example.dive_app.ui.screen.alert.EmergencyScreen
import com.example.dive_app.ui.screen.location.LocationScreen
import com.example.dive_app.ui.viewmodel.FishingPointViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

// ✅ 새 페이지 임포트
import com.example.dive_app.ui.screen.tide.TideDetailTimesPage
import com.example.dive_app.ui.screen.tide.TideDetailSunMoonPage

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

        Scaffold { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") { HomeScreen(navController) }
                    composable("location") { LocationScreen(navController, fishingVM, locationVM) }

                    composable("fishingDetail") {
                        val point = navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<FishingPoint>("fishingPoint")
                        if (point != null) {
                            FishingDetailScreen(point)
                        }
                    }

                    composable("tide") { TideWatchScreen(navController = navController, tideVM = tideVM) }

                    // 🔁 기존 "tideDetail" 제거하고 두 개로 분리
                    composable("tide/times") {
                        val tide = navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<TideInfoData>("selectedTide")
                        if (tide != null) {
                            TideDetailTimesPage(tide = tide, navController = navController)
                        }
                    }
                    composable("tide/sunmoon") {
                        val tide = navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<TideInfoData>("selectedTide")
                        if (tide != null) {
                            TideDetailSunMoonPage(tide = tide, navController = navController)
                        }
                    }

                    composable("emergency") { EmergencyScreen(navController) }
                    composable("weather") { WeatherScreen(navController = navController, weatherVM) }
                    composable("weatherMenu") { WeatherMenuScreen(navController) }
                    composable("sea_weather") { SeaWeatherScreen(navController, weatherVM) }
                    composable("air_quality") { AirQualityScreen(navController, airQualityVM) }
                    composable("health") { HealthScreen(healthVM) }
                }
            }
        }
    }
}
