package com.example.dive_app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dive_app.MainActivity
import com.example.dive_app.domain.viewmodel.WeatherViewModel

@Composable
fun WeatherScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel,
    showDetailArrows: Boolean = true   // ✅ 추가
) {
    val context = LocalContext.current
    val uiState by weatherViewModel.uiState
    LaunchedEffect(Unit) { (context as MainActivity).requestWeather() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE9A7),
                        Color(0xFFFFD456).copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        // 메인 날씨 정보
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Icon(
                imageVector = Icons.Filled.WbSunny,
                contentDescription = "날씨 아이콘",
                tint = Color(0xFFDAA500),
                modifier = Modifier
                    .size(60.dp)
                    .padding(top = 9.dp, bottom = 2.dp)
            )
            Text(
                text = uiState.sky,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = uiState.temp,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherInfoItem(Icons.Filled.Air, "풍속", uiState.windspd)
                WeatherInfoItem(Icons.Filled.Umbrella, "강수확률", uiState.rain)
            }

            Spacer(Modifier.height(5.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherInfoItem(Icons.Filled.Explore, "풍향", uiState.winddir)
                WeatherInfoItem(Icons.Filled.WaterDrop, "습도", uiState.humidity)
            }
        }

        // ◀/▶ 아이콘 — 낚시모드에선 숨김
        if (showDetailArrows) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전",
                tint = Color(0xBCFFFFFF),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("sea_weather") {
                            launchSingleTop = true
                            popUpTo("weather") { inclusive = false }
                        }
                    }
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음",
                tint = Color(0xBCFFFFFF),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("air_quality") {
                            launchSingleTop = true
                            popUpTo("weather") { inclusive = false }
                        }
                    }
            )
        }
    }
}

@Composable
fun WeatherInfoItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.Black,
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 4.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}
