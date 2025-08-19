package com.example.dive_app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 오늘 날짜 가져오기
        val today = LocalDate.now()

        // "Mon Jun" 형식 (영문 약식 요일 + 월)
        val dayMonthFormatter = DateTimeFormatter.ofPattern("EEE MMM", Locale.ENGLISH)
        val dayMonth = today.format(dayMonthFormatter)
        val dayOfMonth = today.dayOfMonth.toString()
        // 중앙 날짜 & 숫자
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-25).dp), // 위로 25dp 이동
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayMonth,   // 오늘 요일 + 월
                color = Color.White,
                fontSize = 18.sp
            )
            Text(
                text = dayOfMonth, // 오늘 일자
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 아이콘 반원 배치
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val radius = 60.dp // 반원 반지름
            val angles = listOf(168f, 115f, 65f, 12f)

            val icons = listOf(
                Triple(Icons.Filled.LocationOn, Color(0xFF4CAF50)) {
                    navController.navigate("location")
                    (context as MainActivity).requestPoint()
                },

                Triple(Icons.Filled.WbSunny, Color(0xFFFFC107), {
                    (context as MainActivity).requestWeather()
                    navController.navigate("weather")
                }),
                Triple(Icons.Filled.Waves, Color(0xFF2196F3), {
                    (context as MainActivity).requestTide()
                    navController.navigate("tide")
                }),
                Triple(Icons.Filled.Favorite, Color(0xFFF44336), {
                    navController.navigate("health")
                })
            )

            icons.forEachIndexed { index, (icon, bg, click) ->
                val angleRad = Math.toRadians(angles[index].toDouble())
                val x = (radius.value * cos(angleRad)).dp
                val y = (radius.value * sin(angleRad)).dp

                Box(
                    modifier = Modifier
                        .offset(x = x, y = y)
                ) {
                    CircleIconButton(
                        icon = icon,
                        background = bg,
                        onClick = click
                    )
                }
            }
        }
    }
}




