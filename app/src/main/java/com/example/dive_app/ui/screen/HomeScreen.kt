// HomeScreen.kt
package com.example.dive_app.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import com.example.dive_app.MainActivity
import com.example.dive_app.ui.component.CircleIconButton
import kotlin.math.cos
import kotlin.math.sin
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.hypot
import androidx.compose.ui.graphics.TransformOrigin
import com.example.dive_app.ui.nav.NavTransitionState

private const val ICON_STAGGER_MS = 180      // 아이콘 간 간격
private const val ICON_DURATION_MS = 1100    // 한 아이콘이 자리 잡는 시간
private val SmoothEasing = FastOutSlowInEasing

private data class RevealParams(
    val center: Offset,
    val color: Color,
)

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current


    // 날짜 포맷
    val today = remember { LocalDate.now() }
    val dayMonthFormatter = remember { DateTimeFormatter.ofPattern("EEE MMM", Locale.ENGLISH) }
    val dayMonth = remember(today) { today.format(dayMonthFormatter) }
    val dayOfMonth = remember(today) { today.dayOfMonth.toString() }

    // 중앙 날짜 애니메이션
    val textAlpha = remember { Animatable(0f) }
    val textTransY = remember { Animatable(40f) }
    LaunchedEffect(Unit) {
        textAlpha.animateTo(1f, tween(450, easing = LinearOutSlowInEasing))
        textTransY.animateTo(0f, tween(500, easing = LinearOutSlowInEasing))
    }

    // 🔵 원형 리빌 상태
    var reveal by remember { mutableStateOf<RevealParams?>(null) }
    val radius = remember { Animatable(0f) }
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenW = with(density) { config.screenWidthDp.dp.toPx() }
    val screenH = with(density) { config.screenHeightDp.dp.toPx() }
    val maxRadiusFor = remember(screenW, screenH) {
        // 화면을 충분히 덮게 대각선 길이를 사용
        hypot(screenW, screenH)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-25).dp)
                .graphicsLayer {
                    alpha = textAlpha.value
                    translationY = textTransY.value
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = dayMonth, color = Color.White, fontSize = 18.sp)
            Text(text = dayOfMonth, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        }

        // 반원 아이콘 스태거 등장
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val radius = 55.dp
                val angles = listOf(168,115,65,12)

                val icons = listOf(
                    Triple(Icons.Filled.LocationOn, Color(0xFF4CAF50)) {
                        (context as MainActivity).requestPoint()
                        context.requestLocation()
                        navController.navigate("location")
                    },
                    Triple(Icons.Filled.WbSunny, Color(0xFFFFC107)) {
                        (context as MainActivity).requestWeather()
                        navController.navigate("weather")
                    },
                    Triple(Icons.Filled.Waves, Color(0xFF2196F3)) {
                        (context as MainActivity).requestTide()
                        navController.navigate("tide")
                    },
                    Triple(Icons.Filled.Favorite, Color(0xFFF44336)) {
                        navController.navigate("health")
                    }
                )

                icons.forEachIndexed { index, (icon, bg, go) ->
                    val angleRad = Math.toRadians(angles[index].toDouble())
                    val targetX = (radius.value * cos(angleRad)).dp
                    val targetY = (radius.value * sin(angleRad)).dp

                    val progress = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        delay((index * ICON_STAGGER_MS).toLong())
                        progress.animateTo(
                            1f, animationSpec = tween(ICON_DURATION_MS, easing = SmoothEasing)
                        )
                    }
                    val scale = lerp(0.85f, 1f, progress.value)
                    val alpha = progress.value
                    val x = (targetX.value * progress.value).dp
                    val y = (targetY.value * progress.value).dp

                    // ⬇️ 이 아이콘의 "화면 기준 중심" 좌표(px) 추적
                    var center by remember { mutableStateOf<Offset?>(null) }

                    Box(
                        modifier = Modifier
                            .offset(x, y)
                            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInRoot()
                                val sz = coords.size
                                center = Offset(pos.x + sz.width / 2f, pos.y + sz.height / 2f)
                            }
                    ) {
                        CircleIconButton(
                            icon = icon,
                            background = bg,
                            onClick = {
                                // ⬇️ 클릭 순간의 transform origin(0..1 비율) 저장
                                val c = center
                                if (c != null && screenW > 0f && screenH > 0f) {
                                    val fx = (c.x / screenW).coerceIn(0f, 1f)
                                    val fy = (c.y / screenH).coerceIn(0f, 1f)
                                    NavTransitionState.origin = TransformOrigin(fx, fy)
                                } else {
                                    NavTransitionState.origin = TransformOrigin.Center
                                }
                                go() // 실제 네비게이션 호출 (위에서 정의한 요청 + navigate)
                            }
                        )
                    }
                }
            }
        }

        // 🔵 원형 리빌 오버레이(맨 위에 그림)
        reveal?.let { rp ->
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = rp.color,
                    radius = radius.value,
                    center = rp.center
                )
            }
        }
    }
}

private fun lerp(start: Float, stop: Float, f: Float): Float =
    start * (1 - f) + stop * f
