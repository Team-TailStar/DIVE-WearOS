package com.example.dive_app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dive_app.domain.model.FishingPoint

@Composable
fun FishingDetailScreen(point: FishingPoint) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 상단 주소
        Text(
            text = point.name,
            fontSize = 12.sp,
            color = Color.Gray
        )

        // 포인트 명
        Text(
            text = point.point_nm,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(Modifier.height(8.dp))

        // 기본 정보 (거리 / 수심 / 저질 / 물때)
        InfoRow("거리", point.point_dt)
        InfoRow("수심", point.dpwt)
        InfoRow("저질", point.material)
        InfoRow("적정 물때", point.tide_time)
        InfoRow("대상 어종", point.target)

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (value.isNotBlank()) value else "-",
            fontSize = 14.sp,
            color = Color.White
        )
    }
}


@Composable
fun InfoSection(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4FC3F7)
        )
        Text(
            text = if (content.isNotBlank()) content else "정보 없음",
            fontSize = 13.sp,
            color = Color.White,
        )
    }
}
