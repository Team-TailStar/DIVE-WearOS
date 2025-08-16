package com.example.myapplication

import WeatherRepository
import WeatherScreen
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.common.theme.MyApplicationTheme
import com.example.myapplication.domain.model.FishingPoint
import com.example.myapplication.ui.screen.FishingDetailPage
import com.example.myapplication.ui.screen.HomeScreen
import com.example.myapplication.ui.screen.LocationScreen
import com.example.myapplication.ui.screen.WeatherMenuScreen

import com.google.gson.Gson

@Composable
fun MainApp() {
    MyApplicationTheme {
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

            composable("weather") {
                WeatherScreen(navController)
            }
            composable("weatherMenu") { WeatherMenuScreen(navController) }
        }
    }
}
