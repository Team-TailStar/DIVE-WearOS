package com.example.myapplication.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.repository.WearDataRepository
import com.example.myapplication.domain.model.TideViewModel
import com.example.myapplication.domain.model.WeatherViewModel
import com.example.myapplication.ui.viewmodel.FishingPointViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationScreen(navController: NavController) {
    val context = LocalContext.current
    val fishingViewModel: FishingPointViewModel = viewModel()
    val weatherViewModel: WeatherViewModel = viewModel()
    val tideViewModel: TideViewModel = viewModel()

    // Repository 생성 (ViewModel 주입)
    val repository = remember {
        WearDataRepository(context, fishingViewModel, weatherViewModel, tideViewModel)
    }

    LaunchedEffect(Unit) { repository.registerListener() }
    DisposableEffect(Unit) { onDispose { repository.unregisterListener() } }

    val points by fishingViewModel.points.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { 1 + points.size } // 페이지 개수
    )

    HorizontalPager(
        state = pagerState
    ) { page ->
        if (page == 0) {
            CurrentLocationPage()
        } else {
            FishingPointPage(points[page - 1], navController)
        }
    }
}
