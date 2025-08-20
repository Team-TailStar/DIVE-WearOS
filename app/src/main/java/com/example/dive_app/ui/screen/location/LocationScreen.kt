package com.example.dive_app.ui.screen.location

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.ui.viewmodel.FishingPointViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationScreen(
    navController: NavController,
    fishingViewModel: FishingPointViewModel,
    locationViewModel: LocationViewModel
) {
    val points by fishingViewModel.points.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { 1 + points.size } // 페이지 개수
    )

    HorizontalPager(state = pagerState) { page ->
        if (page == 0) {
            CurrentLocationScreen(locationViewModel)
        } else {
            val point = points[page - 1]
            FishingPointScreen(point, navController)
        }
    }
}
