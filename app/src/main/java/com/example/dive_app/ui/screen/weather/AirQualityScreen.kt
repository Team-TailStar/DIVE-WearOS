package com.example.dive_app.ui.screen.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dive_app.MainActivity
import com.example.dive_app.domain.viewmodel.AirQualityViewModel

@Composable
fun AirQualityScreen(
    navController: NavController,
    airQualityViewModel: AirQualityViewModel
) {
    val context = LocalContext.current
    val uiState by airQualityViewModel.uiState
    var selectedType by remember { mutableStateOf("PM10") }

    LaunchedEffect(Unit) {
        (context as MainActivity).requestAirQuality()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ 상단 선택 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AirQualityChip("PM2.5", selectedType) { selectedType = it }
            AirQualityChip("PM10", selectedType) { selectedType = it }
            AirQualityChip("O3", selectedType) { selectedType = it }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ 구분선
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.5f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        val selectedGrade = when (selectedType) {
            "PM2.5" -> uiState.pm25Grade
            "PM10" -> uiState.pm10Grade
            "O3" -> uiState.o3Grade
            else -> 0
        }
        val selectedValue = when (selectedType) {
            "PM2.5" -> uiState.pm25Value
            "PM10" -> uiState.pm10Value
            "O3" -> uiState.o3Value
            else -> 0
        }
        val (statusText, statusColor) = getAirQualityStatus(selectedGrade)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(Color.DarkGray, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = selectedValue.toString(),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ 상태 표시 (예시)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(statusColor, RoundedCornerShape(50))
                .padding(vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = selectedType, // 항목명 (PM2.5, PM10, O3)
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = statusText, // → "좋음", "보통", "나쁨", "매우 나쁨"
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun getAirQualityStatus(grade: Int): Pair<String, Color> {
    return when (grade) {
        1 -> "좋음" to Color(0xFF4CAF50)   // 초록
        2 -> "보통" to Color(0xFFFFC107)   // 노랑
        3 -> "나쁨" to Color(0xFFFF5722)   // 주황
        4 -> "매우 나쁨" to Color(0xFFF44336) // 빨강
        else -> "정보 없음" to Color.Gray
    }
}

@Composable
fun AirQualityChip(
    label: String,
    selected: String,
    onClick: (String) -> Unit
) {
    val isSelected = label == selected
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Color.Blue else Color.DarkGray, // ✅ 선택 여부 색상
                CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clickable { onClick(label) }, // ✅ 클릭 가능
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

