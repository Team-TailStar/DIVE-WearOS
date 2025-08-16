package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AirQualityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ 상단 공기질 선택 버튼 (PM2.5, PM10, O3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp), // ← 위에서 조금 내려줌
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AirQualityChip("PM2.5")
            AirQualityChip("PM10")
            AirQualityChip("O3")
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

        // ✅ 선택된 측정치 표시
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
                    text = "PM10   19µg/㎥",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ 미세먼지 상태 표시
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red, RoundedCornerShape(50))
                .padding(vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "미세먼지",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "매우 나쁨",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AirQualityChip(label: String) {
    Box(
        modifier = Modifier
            .background(Color.DarkGray, CircleShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

