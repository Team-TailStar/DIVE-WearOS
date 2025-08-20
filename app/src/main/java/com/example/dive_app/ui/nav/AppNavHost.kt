// app/src/main/java/com/example/dive_app/ui/nav/AppNavHost.kt
package com.example.dive_app.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

// 모든 스크린을 동일 패키지로 통일했다고 했으므로 한 줄로 임포트 OK
import com.example.dive_app.ui.screen.*

// ViewModels
import com.example.dive_app.domain.viewmodel.AirQualityViewModel
import com.example.dive_app.domain.viewmodel.HealthViewModel
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.domain.viewmodel.TideViewModel
import com.example.dive_app.domain.viewmodel.WeatherViewModel
import com.example.dive_app.ui.viewmodel.FishingPointViewModel

// Models (SavedStateHandle로 전달/회수)
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
        // Home
        composable(
            route = "home",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition  = { fadeOut(tween(250)) }
        ) { HomeScreen(navController) }

        // Weather
        composable(
            route = "weather",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
            }
        ) { WeatherScreen(navController, weatherViewModel) }

        // Weather menu (fade + scale)
        composable(
            route = "weatherMenu",
            enterTransition = { fadeIn(tween(250)) + scaleIn(tween(250), initialScale = 0.96f) },
            exitTransition  = { fadeOut(tween(200)) }
        ) { WeatherMenuScreen(navController) }

        // Sea weather (Up)
        composable(
            route = "sea_weather",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(450)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(350)
                )
            }
        ) { SeaWeatherScreen(navController, weatherViewModel) }

        // Air quality (fade)
        composable(
            route = "air_quality",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition  = { fadeOut(tween(200)) }
        ) { AirQualityScreen(navController, airQualityViewModel) }

        // Tide (Up)
        composable(
            route = "tide",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400)
                )
            }
        ) { TideWatchScreen(navController, tideViewModel) }

        // Tide detail (Up, 조금 더 부드럽게)
        composable(
            route = "tideDetail",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
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

        // Health (scale + fade)
        composable(
            route = "health",
            enterTransition = { scaleIn(tween(400), initialScale = 0.92f) + fadeIn(tween(300)) },
            exitTransition  = { fadeOut(tween(220)) }
        ) { HealthScreen(viewModel = healthViewModel) }

        // Location (Right)
        composable(
            route = "location",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(450)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
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

        // Current location (fade)
        composable(
            route = "current_location",
            enterTransition = { fadeIn(tween(250)) },
            exitTransition  = { fadeOut(tween(200)) }
        ) { CurrentLocationScreen(locationViewModel) }

        // Fishing detail (Left)
        composable(
            route = "fishing_detail",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(450)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
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

        // Fishing point (Left, 덜 밀리게)
        composable(
            route = "fishing_point",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(450)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
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

        // Danger alert (fade)
        composable(
            route = "danger_alert",
            enterTransition = { fadeIn(tween(200)) },
            exitTransition  = { fadeOut(tween(200)) }
        ) { DangerAlertScreen(onCancelClick = { navController.popBackStack() }) }
    }
}
