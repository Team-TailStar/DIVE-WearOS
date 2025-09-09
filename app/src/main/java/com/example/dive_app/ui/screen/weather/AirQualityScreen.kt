package com.example.dive_app.ui.screen

import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dive_app.MainActivity
import com.example.dive_app.domain.viewmodel.AirQualityViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.offset

@Composable
private fun ValueLine(valueText: String, unit: String) {
    val base = TextStyle(
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
        lineHeight = 18.sp,
    )
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(20.dp)) {
        Text(valueText, style = base)
        Spacer(Modifier.width(4.dp))
        if (unit == "µg/m³") {
            Text("µg/m", style = base)
            Text("3", style = base.copy(fontSize = 10.sp), modifier = Modifier.offset(y = (-2).dp))
        } else {
            Text(unit, style = base)
        }
    }
}

@Composable
fun AirQualityScreen(
    navController: NavController,
    airQualityViewModel: AirQualityViewModel,
    showDetailArrows: Boolean = true   // ✅ 추가: 낚시모드에서 화살표 숨김 제어
) {
    val context = LocalContext.current
    val uiState by airQualityViewModel.uiState
    var selectedType by remember { mutableStateOf("PM10") }

    LaunchedEffect(Unit) { (context as MainActivity).requestAirQuality() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        // ◀/▶ 화살표는 플래그로 노출 제어
        if (showDetailArrows) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("weather") {
                            launchSingleTop = true
                            popUpTo("air_quality") { inclusive = false } // ✅ 라우트명 통일
                        }
                    }
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("sea_weather") {
                            launchSingleTop = true
                            popUpTo("air_quality") { inclusive = false } // ✅ 라우트명 통일
                        }
                    }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.70f)
                .padding(top = 32.dp, bottom = 6.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.M.d")),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 11.dp)
            )

            MetricRow(
                leftTitle = "초미세먼지", leftSub = "PM2.5",
                grade = uiState.pm25Grade, isSelected = selectedType == "PM2.5",
                onClick = { selectedType = "PM2.5" }
            )
            Spacer(Modifier.height(5.dp))
            MetricRow(
                leftTitle = "미세먼지", leftSub = "PM10",
                grade = uiState.pm10Grade, isSelected = selectedType == "PM10",
                onClick = { selectedType = "PM10" }
            )
            Spacer(Modifier.height(5.dp))
            MetricRow(
                leftTitle = "오존", leftSub = "O₃",
                grade = uiState.o3Grade, isSelected = selectedType == "O3",
                onClick = { selectedType = "O3" }
            )
        }

        val (valueText, unit, title) = when (selectedType) {
            "PM2.5" -> if (uiState.pm25Grade == 0) Triple("정보 없음", "", "초미세먼지")
            else Triple(uiState.pm25Value.toString(), "µg/m³", "초미세먼지")
            "PM10"  -> if (uiState.pm10Grade == 0) Triple("정보 없음", "", "미세먼지")
            else Triple(uiState.pm10Value.toString(), "µg/m³", "미세먼지")
            else    -> if (uiState.o3Grade == 0) Triple("정보 없음", "", "오존")
            else Triple(uiState.o3Value.toString(), "ppm", "오존")
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
        ) {
            BottomInfoPill(title, valueText, unit)
        }
    }
}

@Composable
private fun MetricRow(
    leftTitle: String,
    leftSub: String,
    grade: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (label, color) = gradeToStatus(grade)
    val itemHeight = 26.dp
    val leftWidth  = 66.dp
    val rightWidth = 95.dp

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(leftWidth)
                .height(itemHeight)
                .background(
                    if (isSelected) Color(0xFF006FC7) else Color(0xFF2B2B2B),
                    RoundedCornerShape(50)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(leftTitle, color = Color.White, fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, maxLines = 1)
                Text(leftSub, color = Color(0xFFBEBEBE), fontSize = 7.sp, maxLines = 1)
            }
        }
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .width(rightWidth)
                .height(itemHeight)
                .background(color, RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun BottomInfoPill(title: String, valueText: String, unit: String) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val w = maxWidth
        val h = w / 2
        val shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp, bottomStart = h, bottomEnd = h)
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .width(w * 0.52f)
                    .height(h * 0.47f)
                    .clip(shape)
                    .background(Color(0xF01A1A1A))
                    .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF343434), RoundedCornerShape(50.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(title, color = Color.White, fontSize = 8.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(1.dp))
                    if (valueText == "정보 없음") {
                        Text("정보 없음", color = Color.White, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                    } else {
                        ValueLine(valueText, unit)
                    }
                }
            }
        }
    }
}

private fun gradeToStatus(grade: Int): Pair<String, Color> = when (grade) {
    1 -> "매우 좋음" to Color(0xFF28A745)
    2 -> "보통"     to Color(0xFFFFBF00)
    3 -> "나쁨"     to Color(0xFFFF7043)
    4 -> "매우 나쁨" to Color(0xFFE53935)
    else -> "정보 없음" to Color(0xFF616161)
}
