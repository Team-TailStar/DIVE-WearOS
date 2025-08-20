package com.example.dive_app.ui.nav

import androidx.compose.runtime.Composable

// Accompanist animated navigation
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import androidx.compose.animation.*

// Animation specs & easings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

// Screens
import com.example.dive_app.ui.screen.*

// ViewModels (패키지 주의: FishingPointViewModel은 ui.viewmodel)
import com.example.dive_app.domain.viewmodel.AirQualityViewModel
import com.example.dive_app.domain.viewmodel.HealthViewModel
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.domain.viewmodel.TideViewModel
import com.example.dive_app.domain.viewmodel.WeatherViewModel
import com.example.dive_app.ui.viewmodel.FishingPointViewModel

// Models (SavedStateHandle로 전달)
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.model.TideInfoData

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
        startDestination = "home"
    ) {
        composable(
            route = "home",
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition  = { fadeOut(animationSpec = tween(250)) }
        ) { HomeScreen(navController) }

        composable(
            route = "weather",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            },
            exitTransition  = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            }
        ) { WeatherScreen(navController, weatherViewModel) }

        composable(
            route = "weatherMenu",
            enterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        scaleIn(animationSpec = tween(250), initialScale = 0.96f)
            },
            exitTransition  = { fadeOut(animationSpec = tween(200)) }
        ) { WeatherMenuScreen(navController) }

        composable(
            route = "sea_weather",
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(450)
                )
            },
            exitTransition  = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(350)
                )
            }
        ) { SeaWeatherScreen(navController, weatherViewModel) }

        composable(
            route = "air_quality",
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition  = { fadeOut(animationSpec = tween(200)) }
        ) { AirQualityScreen(navController, airQualityViewModel) }

        composable(
            route = "tide",
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500)
                )
            },
            exitTransition  = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(400)
                )
            }
        ) { TideWatchScreen(navController, tideViewModel) }

        composable(
            route = "tideDetail",
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                )
            },
            exitTransition  = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(350)
                )
            }
        ) {
            val tide: TideInfoData? =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<TideInfoData>("selectedTide")
            tide?.let { TideDetailPage(tide = it, navController = navController) }
        }

        composable(
            route = "health",
            enterTransition = {
                scaleIn(animationSpec = tween(400), initialScale = 0.92f) +
                        fadeIn(animationSpec = tween(300))
            },
            exitTransition  = { fadeOut(animationSpec = tween(220)) }
        ) { HealthScreen(viewModel = healthViewModel) }

        composable(
            route = "location",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(450)
                )
            },
            exitTransition  = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350)
                )
            }
        ) {
            LocationScreen(
                navController = navController,
                fishingViewModel = fishViewModel,
                locationViewModel = locationViewModel
            )
        }

        composable(
            route = "current_location",
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition  = { fadeOut(animationSpec = tween(200)) }
        ) { CurrentLocationScreen(locationViewModel) }

        composable(
            route = "fishing_detail",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(450)
                )
            },
            exitTransition  = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(350)
                )
            }
        ) {
            val point: FishingPoint? =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<FishingPoint>("selectedPoint")
            point?.let { FishingDetailScreen(point = it) }
        }

        composable(
            route = "fishing_point",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it / 2 },
                    animationSpec = tween(450)
                )
            },
            exitTransition  = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = tween(350)
                )
            }
        ) {
            val point: FishingPoint? =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<FishingPoint>("selectedPoint")
            point?.let { FishingPointScreen(point = it, navController = navController) }
        }

        composable(
            route = "danger_alert",
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition  = { fadeOut(animationSpec = tween(200)) }
        ) { DangerAlertScreen(onCancelClick = { navController.popBackStack() }) }
    }
}
