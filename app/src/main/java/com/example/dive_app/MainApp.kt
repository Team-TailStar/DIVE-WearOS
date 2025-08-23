package com.example.dive_app

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.*
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.SwipeToDismissValue // ← 프로젝트 버전에 맞게 (deprecated 경고만)
import androidx.wear.compose.material.rememberSwipeToDismissBoxState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.distinctUntilChanged

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
                        SwipeDismissContainer(
                            onDismiss = {
                                if (mode == AppMode.FISHING) dismissToMode(navController)
                                else context.finish()
                            }
                        ) {
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

                    // tide -> tide/times
                    composable("tide") {
                        SwipeDismissContainer(
                            onDismiss = {
                                if (mode == AppMode.FISHING) dismissToMode(navController) else dismissToHome(navController)
                            }
                        ) {
                            if (mode == AppMode.FISHING) {
                                AutoAdvancePage(
                                    mode = mode,
                                    navController = navController,
                                    nextRoute = "tide/times",
                                    beforeNavigate = {
                                        // times 화면이 읽을 수 있도록 오늘 데이터를 저장
                                        tideVM.uiState.value.tideList.firstOrNull()?.let { t ->
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle?.set("selectedTide", t)
                                        }
                                    }
                                ) {
                                    TideWatchScreen(navController, tideVM, showDetailArrows = false)
                                }
                            } else {
                                TideWatchScreen(navController, tideVM)
                            }
                        }
                    }

                    // tide/times -> tide/sunmoon
                    composable("tide/times") {
                        SwipeDismissContainer(
                            onDismiss = {
                                if (mode == AppMode.FISHING) dismissToMode(navController) else dismissToHome(navController)
                            }
                        ) {
                            val tide =
                                navController.previousBackStackEntry?.savedStateHandle?.get<TideInfoData>("selectedTide")
                            if (tide != null) {
                                if (mode == AppMode.FISHING) {
                                    AutoAdvancePage(
                                        mode = mode,
                                        navController = navController,
                                        nextRoute = "tide/sunmoon",
                                        beforeNavigate = {
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle?.set("selectedTide", tide)
                                        }
                                    ) {
                                        TideDetailTimesPage(tide, navController)
                                    }
                                } else {
                                    TideDetailTimesPage(tide, navController)
                                }
                            }
                        }
                    }

                    // tide/sunmoon -> air_quality
                    composable("tide/sunmoon") {
                        SwipeDismissContainer(
                            onDismiss = {
                                if (mode == AppMode.FISHING) dismissToMode(navController) else dismissToHome(navController)
                            }
                        ) {
                            val tide =
                                navController.previousBackStackEntry?.savedStateHandle?.get<TideInfoData>("selectedTide")
                            if (tide != null) {
                                if (mode == AppMode.FISHING) {
                                    AutoAdvancePage(
                                        mode = mode,
                                        navController = navController,
                                        nextRoute = "air_quality"
                                    ) {
                                        TideDetailSunMoonPage(tide, navController)
                                    }
                                } else {
                                    TideDetailSunMoonPage(tide, navController)
                                }
                            }
                        }
                    }

                    // air_quality -> sea_weather -> weather -> tide (loop)
                    composable("air_quality") {
                        SwipeDismissContainer(
                            onDismiss = {
                                if (mode == AppMode.FISHING) dismissToMode(navController) else dismissToHome(navController)
                            }
                        ) {
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
                        SwipeDismissContainer(
                            onDismiss = {
                                if (mode == AppMode.FISHING) dismissToMode(navController) else dismissToHome(navController)
                            }
                        ) {
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
                        SwipeDismissContainer(
                            onDismiss = {
                                if (mode == AppMode.FISHING) dismissToMode(navController) else dismissToHome(navController)
                            }
                        ) {
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

                    composable("location") {
                        SwipeDismissContainer(
                            onDismiss = {
                                if (mode == AppMode.FISHING) dismissToMode(navController) else dismissToHome(navController)
                            }
                        ) {
                            LocationScreen(navController, fishingVM, locationVM)
                        }
                    }
                    composable("emergency") {
                        SwipeDismissContainer(
                            onDismiss = {
                                if (mode == AppMode.FISHING) dismissToMode(navController) else dismissToHome(navController)
                            }
                        ) {
                            EmergencyScreen(navController)
                        }
                    }
                    composable("health") {
                        SwipeDismissContainer(
                            onDismiss = {
                                if (mode == AppMode.FISHING) dismissToMode(navController) else dismissToHome(navController)
                            }
                        ) {
                            HealthScreen(healthVM)
                        }
                    }
                    composable("fishingDetail") {
                        SwipeDismissContainer(
                            onDismiss = {
                                if (mode == AppMode.FISHING) dismissToMode(navController) else dismissToHome(navController)
                            }
                        ) {
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

/** home로 이동 헬퍼 */
private fun dismissToHome(navController: NavController) {
    if (!navController.popBackStack("home", false)) {
        navController.navigate("home") {
            launchSingleTop = true
            popUpTo("home") { inclusive = false }
        }
    }
}

/** mode로 이동 헬퍼 */
private fun dismissToMode(navController: NavController) {
    if (!navController.popBackStack("mode", false)) {
        navController.navigate("mode") {
            launchSingleTop = true
            popUpTo("mode") { inclusive = false }
        }
    }
}

/** 낚시 모드: 자동 순환 + 탭으로 일시정지/재개 + 아래로 드래그하면 location 이동 */
@Composable
private fun AutoAdvancePage(
    mode: AppMode,
    navController: NavController,
    nextRoute: String,
    intervalMillis: Long = 3000L,
    beforeNavigate: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var paused by remember { mutableStateOf(false) }
    var dragAccum by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val threshold = with(density) { 72.dp.toPx() }

    // 모드 변경 시 자동 순환 초기화
    LaunchedEffect(mode) { if (mode != AppMode.FISHING) paused = false }

    // 타이머
    LaunchedEffect(mode, nextRoute, paused) {
        if (mode == AppMode.FISHING && !paused) {
            while (isActive && !paused) {
                delay(intervalMillis)
                if (paused || mode != AppMode.FISHING) break
                beforeNavigate?.invoke()
                navController.navigate(nextRoute) {
                    launchSingleTop = true
                    popUpTo("home") { inclusive = false }
                }
            }
        }
    }

    // 콘텐츠 + 제스처 캐처
    Box(Modifier.fillMaxSize()) {
        content()
        if (mode == AppMode.FISHING) {
            // 탭 → 일시정지/재개 + 아래로 드래그 → location
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { paused = !paused })
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dy ->
                                if (dy > 0f) { // 아래로만 축적
                                    dragAccum += dy
                                    if (dragAccum >= threshold) {
                                        dragAccum = 0f
                                        paused = true
                                        navController.navigate("location") {
                                            launchSingleTop = true
                                            popUpTo("home") { inclusive = false }
                                        }
                                    }
                                }
                            },
                            onDragEnd = { dragAccum = 0f },
                            onDragCancel = { dragAccum = 0f }
                        )
                    }
            )
        }
    }
}

/** 스와이프 백/하드웨어 백 통합 처리 (프로젝트 버전에 맞춰 SwipeToDismissValue 사용) */
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

    BackHandler { onDismiss() }

    SwipeToDismissBox(state = state) { content() }
}
