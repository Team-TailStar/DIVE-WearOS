package com.example.dive_app

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.SwipeToDismissValue
import androidx.wear.compose.material.rememberSwipeToDismissBoxState
import com.example.dive_app.common.theme.MyApplicationTheme
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.model.TideInfoData
import com.example.dive_app.domain.viewmodel.*
import com.example.dive_app.ui.screen.*
import com.example.dive_app.ui.screen.alert.EmergencyScreen
import com.example.dive_app.ui.screen.location.LocationScreen
import com.example.dive_app.ui.screen.tide.TideDetailSunMoonPage
import com.example.dive_app.ui.screen.tide.TideDetailTimesPage
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.navigation.NavController
import androidx.navigation.*
import com.example.dive_app.ui.viewmodel.FishingPointViewModel

@Composable
fun MainApp(
    healthVM: HealthViewModel,
    fishingVM: FishingPointViewModel,
    weatherVM: WeatherViewModel,
    tideVM: TideViewModel,
    locationVM: LocationViewModel,
    airQualityVM: AirQualityViewModel,
    appModeVM: AppModeViewModel
) {
    MyApplicationTheme {
        val navController = rememberSwipeDismissableNavController()
        val context = LocalContext.current as ComponentActivity
        val mode by appModeVM.mode.collectAsState()

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
                    startDestination = "mode" // 홈 대신 모드 화면부터
                ) {
                    // 0) 모드 선택
                    composable("mode") {
                        SwipeDismissContainer(onDismiss = { context.finish() }) {
                            ModeScreen(
                                navController = navController,
                                appModeVM = appModeVM,
                            )

                        }
                    }

                    // 1) 홈
                    composable("home") {
                        SwipeDismissContainer(onDismiss = { context.finish() }) {
                            // 낚시 모드면 홈을 스킵하고 tide로 보냄
                            LaunchedEffect(mode) {
                                if (mode == AppMode.FISHING) {
                                    navController.navigate("tide") {
                                        launchSingleTop = true
                                        popUpTo("home") { inclusive = false }
                                    }
                                }
                            }
                            if (mode == AppMode.NORMAL) {
                                HomeScreen(navController)
                            }
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

                    // 2) 순환 대상 라우트들
                    composable("tide") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            if (mode == AppMode.FISHING) {
                                AutoAdvancePage(
                                    mode = mode,
                                    navController = navController,
                                    nextRoute = "air_quality"
                                ) {
                                    TideWatchScreen(navController, tideVM)
                                }
                            } else {
                                TideWatchScreen(navController, tideVM)
                            }
                        }
                    }
                    composable("air_quality") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            if (mode == AppMode.FISHING) {
                                AutoAdvancePage(
                                    mode = mode,
                                    navController = navController,
                                    nextRoute = "sea_weather"
                                ) {
                                    AirQualityScreen(navController, airQualityVM)
                                }
                            } else {
                                AirQualityScreen(navController, airQualityVM)
                            }
                        }
                    }
                    composable("sea_weather") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            if (mode == AppMode.FISHING) {
                                AutoAdvancePage(
                                    mode = mode,
                                    navController = navController,
                                    nextRoute = "weather"
                                ) {
                                    SeaWeatherScreen(navController, weatherVM)
                                }
                            } else {
                                SeaWeatherScreen(navController, weatherVM)
                            }
                        }
                    }
                    composable("weather") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            if (mode == AppMode.FISHING) {
                                AutoAdvancePage(
                                    mode = mode,
                                    navController = navController,
                                    nextRoute = "tide" // loop
                                ) {
                                    WeatherScreen(navController, weatherVM)
                                }
                            } else {
                                WeatherScreen(navController, weatherVM)
                            }
                        }
                    }

                    // 3) 기타 라우트
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
                    composable("location") {
                        SwipeDismissContainer({ dismissToHome() }) {
                            LocationScreen(navController, fishingVM, locationVM)
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

/** 낚시 모드에서만 3초마다 다음 라우트로 자동 이동 */
@Composable
private fun AutoAdvancePage(
    mode: AppMode,
    navController: NavController,
    nextRoute: String,
    intervalMillis: Long = 3000L,
    content: @Composable () -> Unit
) {
    LaunchedEffect(mode, nextRoute) {
        if (mode == AppMode.FISHING) {
            while (isActive) {
                delay(intervalMillis)
                if (mode != AppMode.FISHING) break
                navController.navigate(nextRoute) {
                    launchSingleTop = true
                    popUpTo("home") { inclusive = false }
                }
            }
        }
    }
    content()
}

@Composable
private fun SwipeDismissContainer(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState()
    LaunchedEffect(state.currentValue) {
        if (state.currentValue != SwipeToDismissValue.Default) {
            onDismiss()
        }
    }
    SwipeToDismissBox(state = state) { content() }
}
