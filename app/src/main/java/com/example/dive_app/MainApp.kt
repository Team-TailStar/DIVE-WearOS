package com.example.dive_app

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.navigation.composable
import com.example.dive_app.common.theme.MyApplicationTheme
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.model.TideInfoData
import com.example.dive_app.domain.viewmodel.*
import com.example.dive_app.ui.screen.*
import com.example.dive_app.ui.screen.alert.EmergencyScreen
import com.example.dive_app.ui.screen.location.LocationScreen
import com.example.dive_app.ui.screen.tide.TideDetailSunMoonPage
import com.example.dive_app.ui.screen.tide.TideDetailTimesPage
import com.example.dive_app.ui.viewmodel.FishingPointViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.SwipeToDismissValue
import androidx.wear.compose.material.rememberSwipeToDismissBoxState

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
        val navController = rememberSwipeDismissableNavController()
        val context = LocalContext.current as ComponentActivity

        // 응급 이벤트 감지 → emergency 화면 이동
        LaunchedEffect(healthVM, navController) {
            healthVM.emergencyEvent.distinctUntilChanged().collect { event ->
                when (event) {
                    is EmergencyEvent.HeartRateLow,
                    is EmergencyEvent.Spo2Low,
                    is EmergencyEvent.ScreenTapped -> {
                        if (navController.currentBackStackEntry?.destination?.route != "emergency") {
                            navController.navigate("emergency") { launchSingleTop = true }
                        }
                    }
                }
            }
        }

        Scaffold { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    // 홈: 스와이프 → 앱 종료
                    composable("home") {
                        SwipeDismissContainer(onDismiss = { context.finish() }) {
                            HomeScreen(navController)
                        }
                    }

                    fun dismissToHome() {
                        if (!navController.popBackStack("home", false)) {
                            navController.navigate("home") {
                                launchSingleTop = true
                                popUpTo("home") { inclusive = false }
                            }
                        }
                    }

                    composable("location") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            LocationScreen(navController, fishingVM, locationVM)
                        }
                    }
                    composable("tide") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            TideWatchScreen(navController, tideVM)
                        }
                    }
                    composable("tide/times") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            val tide = navController.previousBackStackEntry
                                ?.savedStateHandle?.get<TideInfoData>("selectedTide")
                            if (tide != null) TideDetailTimesPage(tide, navController)
                        }
                    }
                    composable("tide/sunmoon") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            val tide = navController.previousBackStackEntry
                                ?.savedStateHandle?.get<TideInfoData>("selectedTide")
                            if (tide != null) TideDetailSunMoonPage(tide, navController)
                        }
                    }
                    composable("weather") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            WeatherScreen(navController, weatherVM)
                        }
                    }
                    composable("sea_weather") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            SeaWeatherScreen(navController, weatherVM)
                        }
                    }
                    composable("air_quality") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            AirQualityScreen(navController, airQualityVM)
                        }
                    }
                    composable("emergency") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            EmergencyScreen(navController)
                        }
                    }
                    composable("health") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            HealthScreen(healthVM)
                        }
                    }
                    composable("fishingDetail") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            val point = navController.previousBackStackEntry
                                ?.savedStateHandle?.get<FishingPoint>("fishingPoint")
                            if (point != null) FishingDetailScreen(point)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeDismissContainer(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState()

    // Default가 아닌 경우(= 해제됨) 콜백 실행
    LaunchedEffect(state.currentValue) {
        if (state.currentValue != SwipeToDismissValue.Default) {
            onDismiss()
        }
    }

    SwipeToDismissBox(state = state) {
        content()
    }
}
