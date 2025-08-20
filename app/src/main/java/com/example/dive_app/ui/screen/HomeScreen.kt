// HomeScreen.kt
package com.example.dive_app.ui.screen

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
import kotlinx.coroutines.delay // ⬅️ 추가

private const val ICON_STAGGER_MS = 180      // 아이콘 간 간격
private const val ICON_DURATION_MS = 1100    // 한 아이콘이 자리 잡는 시간
private val SmoothEasing = FastOutSlowInEasing

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    // 날짜 포맷
    val today = remember { LocalDate.now() }
    val dayMonthFormatter = remember { DateTimeFormatter.ofPattern("EEE MMM", Locale.ENGLISH) }
    val dayMonth = remember(today) { today.format(dayMonthFormatter) }
    val dayOfMonth = remember(today) { today.dayOfMonth.toString() }

    // 중앙 날짜 애니메이션: alpha(0→1), translationY(20px→0px)
    val textAlpha = remember { Animatable(0f) }
    val textTransY = remember { Animatable(40f) }
    LaunchedEffect(Unit) {
        textAlpha.animateTo(1f, tween(450, easing = LinearOutSlowInEasing))
        textTransY.animateTo(0f, tween(500, easing = LinearOutSlowInEasing))
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
                    translationY = textTransY.value // px 단위
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = dayMonth, color = Color.White, fontSize = 18.sp)
            Text(text = dayOfMonth, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        }

        // 반원 아이콘 스태거 등장
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val radius = 55.dp
            val angles = listOf(168,115,65,12)


            val icons = listOf(
                Triple(Icons.Filled.LocationOn, Color(0xFF4CAF50)) {
                    navController.navigate("location")
                    (context as MainActivity).requestPoint()
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

            icons.forEachIndexed { index, (icon, bg, onClick) ->
                val angleRad = Math.toRadians(angles[index].toDouble())
                val targetX = (radius.value * cos(angleRad)).dp
                val targetY = (radius.value * sin(angleRad)).dp

                // 0→1 진행도
                val progress = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    delay((index * ICON_STAGGER_MS).toLong())
                    progress.animateTo(
                        1f,
                        animationSpec = tween(
                            durationMillis = ICON_DURATION_MS, // ⬅️ 더 긴 지속시간
                            easing = SmoothEasing              // ⬅️ 부드러운 커브
                        )
                    )
                }

                // 부드러운 시작을 위해 스케일 시작값 살짝 키움(튐 방지)
                val scale = lerp(0.85f, 1f, progress.value)
                val alpha = progress.value
                val x = (targetX.value * progress.value).dp
                val y = (targetY.value * progress.value).dp

                Box(
                    modifier = Modifier
                        .offset(x, y)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                ) {
                    CircleIconButton(icon = icon, background = bg, onClick = onClick)
                }
            }


        }
    }
}

private fun lerp(start: Float, stop: Float, f: Float): Float =
    start * (1 - f) + stop * f
