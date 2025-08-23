package com.example.dive_app
import androidx.compose.ui.Alignment
import androidx.wear.compose.material.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.zIndex

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
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
        val showArrows = mode != AppMode.FISHING

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
                        LaunchedEffect(Unit) {
                            (context as MainActivity).requestTide()
                            (context as MainActivity).requestLocation()
                            (context as MainActivity).requestWeather()
                            (context as MainActivity).requestPoint()
                        }
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
                            onDismiss = {            // ← 여기만 변경
                                dismissToMode(navController)
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
                                    upSwipeNextRoute = "location",   // ↑ location
                                    downSwipeNextRoute = "health",   // ↓ health
                                    beforeNavigate = {
                                        tideVM.uiState.value.tideList.firstOrNull()?.let { t ->
                                            navController.currentBackStackEntry?.savedStateHandle?.set("selectedTide", t)
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
                            val tide = tideVM.uiState.value.tideList.firstOrNull()

                            if (tide != null) {
                                if (mode == AppMode.FISHING) {
                                    AutoAdvancePage(
                                        mode = mode,
                                        navController = navController,
                                        nextRoute = "tide/sunmoon",
                                        downSwipeNextRoute = "location",
                                        beforeNavigate = {
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle?.set("selectedTide", tide)
                                        }
                                    ) {
                                        TideDetailTimesPage(tide, navController, showDetailArrows = false)
                                    }
                                } else {
                                    TideDetailTimesPage(tide, navController, showDetailArrows = showArrows)
                                }
                            } else {
                                // ★ 데이터 없을 때: 안내 + 낚시모드는 다음 화면으로 자동 이동
                                if (mode == AppMode.FISHING) {
                                    AutoAdvancePage(
                                        mode = mode,
                                        navController = navController,
                                        downSwipeNextRoute = "location",
                                        nextRoute = "air_quality"
                                    ) { MissingTidePlaceholder() }
                                } else {
                                    MissingTidePlaceholder()
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
                            val tide = tideVM.uiState.value.tideList.firstOrNull()

                            if (tide != null) {
                                if (mode == AppMode.FISHING) {
                                    AutoAdvancePage(
                                        mode = mode,
                                        navController = navController,
                                        downSwipeNextRoute = "location",
                                        nextRoute = "air_quality"

                                    ) {
                                        TideDetailSunMoonPage(tide, navController, showDetailArrows = false)
                                    }
                                } else {
                                    TideDetailSunMoonPage(tide, navController, showDetailArrows = showArrows)
                                }
                            } else {
                                // ★ 데이터 없을 때도 같은 처리
                                if (mode == AppMode.FISHING) {
                                    AutoAdvancePage(
                                        mode = mode,
                                        navController = navController,
                                        downSwipeNextRoute = "location",
                                        nextRoute = "air_quality"
                                    ) { MissingTidePlaceholder() }
                                } else {
                                    MissingTidePlaceholder()
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
                                    downSwipeNextRoute = "location",
                                    nextRoute = "sea_weather"
                                ) {
                                    AirQualityScreen(navController, airQualityVM, showDetailArrows = false)
                                }
                            } else {
                                AirQualityScreen(navController, airQualityVM, showDetailArrows = showArrows)
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
                                    downSwipeNextRoute = "location",
                                    nextRoute = "weather"
                                ) {
                                    SeaWeatherScreen(navController, weatherVM, showDetailArrows = false)
                                }
                            } else {
                                SeaWeatherScreen(navController, weatherVM, showDetailArrows = showArrows)
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
                                    downSwipeNextRoute = "location",
                                    nextRoute = "tide" // loop
                                ) {
                                    WeatherScreen(navController, weatherVM, showDetailArrows = false)
                                }
                            } else {
                                WeatherScreen(navController, weatherVM, showDetailArrows = showArrows)
                            }
                        }
                    }
                    composable("location") {
                        SwipeDismissContainer(
                            onDismiss = {
                                if (mode == AppMode.FISHING) dismissToMode(navController) else dismissToHome(navController)
                            }
                        ) {
                            VerticalSwipeNavigate(
                                mode = mode,
                                navController = navController,
                                upRoute = "health",
                                downRoute = "tide"
                            ) {
                                LocationScreen(
                                    navController = navController,
                                    fishingViewModel = fishingVM,
                                    weatherViewModel = weatherVM,
                                    tideViewModel = tideVM,
                                    locationViewModel = locationVM,
                                    // ★ 여기! 앱 전체 모드 == FISHING 전달
                                    isAppFishingMode = (mode == AppMode.FISHING)
                                )
                            }
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
                            VerticalSwipeNavigate(
                                mode = mode,
                                navController = navController,
                                upRoute = "tide",       // ↑ tide
                                downRoute = "location"  // ↓ location
                            ) {
                                HealthScreen(
                                    viewModel = healthVM,
                                    showArrows = false,
                                    autoCycle = true,
                                    intervalMillis = 3000L
                                )
                            }
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
    downSwipeNextRoute: String? = null,   // ↓로 이동할 곳
    upSwipeNextRoute: String? = null,     // ↑로 이동할 곳  ⬅️ 추가
    content: @Composable () -> Unit
) {
    var paused by remember { mutableStateOf(false) }
    var downAccum by remember { mutableStateOf(0f) }
    var upAccum by remember { mutableStateOf(0f) }
    var toast by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current
    val threshold = with(density) { 72.dp.toPx() }

    LaunchedEffect(toast) { if (toast != null) { delay(800); toast = null } }
    LaunchedEffect(mode) { if (mode != AppMode.FISHING) paused = false }

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

    Box(Modifier.fillMaxSize()) {
        content()

        if (mode == AppMode.FISHING) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            val next = !paused
                            paused = next
                            toast = if (next) "⏸" else "▶"
                        }
                    }
                    .pointerInput(upSwipeNextRoute to downSwipeNextRoute) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dy ->
                                when {
                                    dy > 0f && downSwipeNextRoute != null -> { // ↓
                                        downAccum += dy
                                        if (downAccum >= threshold) {
                                            downAccum = 0f; upAccum = 0f
                                            paused = true
                                            navController.navigate(downSwipeNextRoute) {
                                                launchSingleTop = true
                                                popUpTo("home") { inclusive = false }
                                            }
                                        }
                                    }
                                    dy < 0f && upSwipeNextRoute != null -> {   // ↑
                                        upAccum += -dy
                                        if (upAccum >= threshold) {
                                            downAccum = 0f; upAccum = 0f
                                            paused = true
                                            navController.navigate(upSwipeNextRoute) {
                                                launchSingleTop = true
                                                popUpTo("home") { inclusive = false }
                                            }
                                        }
                                    }
                                }
                            },
                            onDragEnd = { downAccum = 0f; upAccum = 0f },
                            onDragCancel = { downAccum = 0f; upAccum = 0f }
                        )
                    }
            )

            AnimatedVisibility(visible = toast != null, enter = fadeIn(), exit = fadeOut()) {
                Text(text = toast ?: "", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            }
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
@Composable
private fun MissingTidePlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("조석 데이터가 없습니다\n휴대폰 연결을 확인하세요")
    }
}
@Composable
private fun VerticalSwipeNavigate(
    mode: AppMode,
    navController: NavController,
    upRoute: String? = null,           // 위로 스와이프 시 이동
    downRoute: String? = null,         // 아래로 스와이프 시 이동
    thresholdDp: Dp = 56.dp,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val threshold = with(density) { thresholdDp.toPx() }
    var accumUp by remember { mutableStateOf(0f) }
    var accumDown by remember { mutableStateOf(0f) }

    Box(Modifier.fillMaxSize()) {
        content()
        if (mode == AppMode.FISHING) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(upRoute, downRoute) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dy ->
                                if (dy < 0f && upRoute != null) {       // ↑
                                    accumUp += -dy
                                    if (accumUp >= threshold) {
                                        accumUp = 0f; accumDown = 0f
                                        navController.navigate(upRoute) {
                                            launchSingleTop = true
                                            popUpTo("home") { inclusive = false }
                                        }
                                    }
                                } else if (dy > 0f && downRoute != null) { // ↓
                                    accumDown += dy
                                    if (accumDown >= threshold) {
                                        accumDown = 0f; accumUp = 0f
                                        navController.navigate(downRoute) {
                                            launchSingleTop = true
                                            popUpTo("home") { inclusive = false }
                                        }
                                    }
                                }
                            },
                            onDragEnd = { accumUp = 0f; accumDown = 0f },
                            onDragCancel = { accumUp = 0f; accumDown = 0f }
                        )
                    }
            )
        }
    }
}
