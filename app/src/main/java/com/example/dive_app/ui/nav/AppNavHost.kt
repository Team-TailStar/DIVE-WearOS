// app/src/main/java/com/example/dive_app/ui/nav/AppNavHost.kt
package com.example.dive_app.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

// 모든 스크린이 같은 패키지라면 한 줄로 OK
import com.example.dive_app.ui.screen.*

// ViewModels
import com.example.dive_app.domain.viewmodel.*
import com.example.dive_app.ui.viewmodel.FishingPointViewModel

// Models (SavedStateHandle로 전달/회수)
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.model.TideInfoData
// 추가 import
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import com.example.dive_app.ui.screen.location.LocationScreen
import com.example.dive_app.ui.screen.location.CurrentLocationScreen
import com.example.dive_app.ui.screen.location.FishingPointScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(
    healthViewModel: HealthViewModel,
    fishViewModel: FishingPointViewModel,
    weatherViewModel: WeatherViewModel,
    tideViewModel: TideViewModel,
    locationViewModel: LocationViewModel,
    airQualityViewModel: AirQualityViewModel
) {
    val navController = rememberAnimatedNavController()

    AnimatedNavHost(
        navController = navController,
        startDestination = "home",
        enterTransition     = { fadeIn(animationSpec = tween(900)) },
        exitTransition      = { fadeOut(animationSpec = tween(900)) },
        popEnterTransition  = { fadeIn(animationSpec = tween(900)) },
        popExitTransition   = { fadeOut(animationSpec = tween(900)) }
    ) {
        composable("home") { HomeScreen(navController) }
        composable("weather") { WeatherScreen(navController, weatherViewModel) }
        composable("weatherMenu") { WeatherMenuScreen(navController) }
        composable("sea_weather") { SeaWeatherScreen(navController, weatherViewModel) }
        composable("air_quality") { AirQualityScreen(navController, airQualityViewModel) }
        composable("tide") { TideWatchScreen(navController, tideViewModel) }
        composable("tideDetail") {
            val tide: TideInfoData? =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<TideInfoData>("selectedTide")
            tide?.let { TideDetailPage(tide = it, navController = navController) }
        }
        composable("health") { HealthScreen(viewModel = healthViewModel) }
        composable("location") {
            LocationScreen(
                navController = navController,
                fishingViewModel = fishViewModel,
                locationViewModel = locationViewModel
            )
//        }
//        composable("current_location") {
//            CurrentLocationScreen(locationViewModel = locationViewModel)
        }


        composable("fishing_detail") {
            val point: FishingPoint? =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<FishingPoint>("selectedPoint")   // ← 키 통일
            point?.let { FishingDetailScreen(point = it) }
        }

        composable("fishing_point") {
            // 선택된 포인트
            val point: FishingPoint? =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<FishingPoint>("selectedPoint")

            // ✅ 주변 포인트 & 현재 위치 가져오기
            val allPoints by fishViewModel.points.collectAsState()
            val loc by locationViewModel.location.observeAsState()
            val lat = loc?.first ?: 0.0
            val lon = loc?.second ?: 0.0

            point?.let {
                FishingPointScreen(
                    point = it,
                    navController = navController,
                    fishingPoints = allPoints,
                    currentLat = lat,
                    currentLon = lon
                )
            }
        }

        composable("danger_alert") { DangerAlertScreen(onCancelClick = { navController.popBackStack() }) }
    }
}
