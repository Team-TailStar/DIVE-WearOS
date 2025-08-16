package com.example.myapplication.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.myapplication.data.repository.FishingPointRepository

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { FishingPointRepository(context) }

    LaunchedEffect(Unit) { repository.registerListener() }
    DisposableEffect(Unit) { onDispose { repository.unregisterListener() } }

    val points = remember { repository.getSamplePoints() }
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
            FishingPointPage(points[page - 1], pagerState)
        }
    }
}
