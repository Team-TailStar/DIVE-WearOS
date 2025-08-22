package com.example.dive_app.ui.screen.location

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.ui.viewmodel.FishingPointViewModel
import kotlinx.coroutines.launch
import com.example.dive_app.domain.model.FishingPoint
import kotlin.math.abs
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationScreen(
    navController: NavController,
    fishingViewModel: FishingPointViewModel,
    locationViewModel: LocationViewModel
) {
    val points by fishingViewModel.points.collectAsState()
    val loc by locationViewModel.location.observeAsState()
    val lat = loc?.first ?: 0.0
    val lon = loc?.second ?: 0.0

    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { 1 + points.size }
    )
    val scope = rememberCoroutineScope()

    // 🔹 Pager 스크롤 가능 여부 (현위치 화면에서는 기본 false)
    var pagerScrollEnabled by remember { mutableStateOf(false) }

    val onMarkerClick: (FishingPoint) -> Unit = { clicked ->
        val eps = 1e-5
        val idx = points.indexOfFirst {
            kotlin.math.abs(it.lat - clicked.lat) < eps &&
                    kotlin.math.abs(it.lon - clicked.lon) < eps
        }.takeIf { it >= 0 } ?: points.indexOfFirst { it.name == clicked.name }
        if (idx >= 0) {
            scope.launch { pagerState.animateScrollToPage(idx + 1) }
        }
    }

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = pagerScrollEnabled   // 🔹 여기!
    ) { page ->
        if (page == 0) {
            CurrentLocationScreen(
                locationViewModel = locationViewModel,
                points = points,
                onMarkerClick = onMarkerClick,
                // 🔹 CHILD → PARENT로 스크롤 허용/차단 토글
                setPagerScrollEnabled = { allow -> pagerScrollEnabled = allow }
            )
        } else {
            val point = points[page - 1]
            FishingPointScreen(
                point = point,
                navController = navController,
                fishingPoints = points,
                currentLat = lat,
                currentLon = lon,
                onMarkerClick = onMarkerClick
            )
        }
    }
}

