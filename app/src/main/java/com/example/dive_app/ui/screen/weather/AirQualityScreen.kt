package com.example.dive_app.ui.screen

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dive_app.MainActivity
import com.example.dive_app.domain.viewmodel.AirQualityViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.layout.Row

@Composable
private fun ValueLine(valueText: String, unit: String) {
    val base = TextStyle(
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 18.sp,
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(20.dp) // ← 고정 높이
    ) {
        Text(valueText, style = base)
        Spacer(Modifier.width(4.dp))
        if (unit == "µg/m³") {
            Text("µg/m", style = base)
            Text(
                "3",
                style = base.copy(fontSize = 10.sp),
                modifier = Modifier.offset(y = (-2).dp)
            )
        } else {
            Text(unit, style = base)
        }
    }
}

@Composable
fun AirQualityScreen(
    navController: NavController,
    airQualityViewModel: AirQualityViewModel
) {
    val context = LocalContext.current
    val uiState by airQualityViewModel.uiState
    var selectedType by remember { mutableStateOf("PM10") }

    LaunchedEffect(Unit) { (context as MainActivity).requestAirQuality() }

    // 원형 화면 안전폭 + 전체 컴팩트 스케일
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        // ⬇ 상단 콘텐츠 (날짜 + 3행만)
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
                fontWeight = FontWeight.SemiBold,
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

        // ⬇ 하단 반원 카드를 화면 **아래 중앙**으로 고정
        val (value, unit, title) = when (selectedType) {
            "PM2.5" -> Triple(uiState.pm25Value, "µg/m³", "초미세먼지")
            "PM10"  -> Triple(uiState.pm10Value, "µg/m³", "미세먼지")
            else    -> Triple(uiState.o3Value,  "ppm",   "오존")
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
        ) {
            BottomInfoPill(title, value.toString(), unit)
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

    // 공통 크기(6개 박스 동일 높이)
    val itemHeight = 26.dp
    val leftWidth  = 66.dp   // 왼쪽 더 짧게
    val rightWidth = 95.dp  // 오른쪽도 소폭 축소

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽 버튼: 짧고 둥근(알약), 텍스트 중앙정렬
        Box(
            modifier = Modifier
                .width(leftWidth)
                .height(itemHeight)
                .background(
                    if (isSelected) Color(0xFF2196F3) else Color(0xFF2B2B2B),
                    RoundedCornerShape(50)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = leftTitle,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = leftSub,           // PM2.5 / PM10 / O₃
                    color = Color(0xFFBEBEBE),
                    fontSize = 7.sp,
                    maxLines = 1
                )
            }
        }

        // 좌우 박스 사이 간격 (작게)
        Spacer(Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .width(rightWidth)
                .height(itemHeight)
                .background(color, RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,             // 매우 좋음 / 보통 / …
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BottomInfoPill(
    title: String,
    valueText: String,
    unit: String
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val w = maxWidth
        val h = w / 2
        val shape = RoundedCornerShape(
            topStart =50.dp, topEnd = 50.dp,
            bottomStart = h, bottomEnd = h
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
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
                        Text(
                            title,
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(1.dp))
                    val styled = buildAnnotatedString {
                        append(valueText); append(" ")
                        if (unit == "µg/m³") {
                            append("µg/m")
                            pushStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 10.sp))
                            append("3")
                            pop()
                        } else append(unit)
                    }
                    ValueLine(valueText, unit)

                }
            }
        }
    }
}

// 등급 매핑(변경 없음)
private fun gradeToStatus(grade: Int): Pair<String, Color> = when (grade) {
    1 -> "매우 좋음" to Color(0xFF28A745)
    2 -> "보통"     to Color(0xFFFFBF00)
    3 -> "나쁨"     to Color(0xFFFF7043)
    4 -> "매우 나쁨" to Color(0xFFE53935)
    else -> "정보 없음" to Color(0xFF616161)
}
