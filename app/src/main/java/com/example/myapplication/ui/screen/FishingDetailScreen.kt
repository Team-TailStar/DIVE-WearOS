package com.example.myapplication.ui.screen

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
import com.example.myapplication.domain.model.FishingPoint

@Composable
fun FishingDetailPage(point: FishingPoint) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // ✅ WearOS 기본 스타일: 블랙 배경
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally, // 👉 전체 중앙 정렬
        verticalArrangement = Arrangement.Top
    ) {
        // 상단 주소
        Text(
            text = point.addr,
            fontSize = 12.sp,
            color = Color.Gray
        )

        // 포인트명
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

        Spacer(Modifier.height(12.dp))

        // 추가 설명 섹션
        if (point.intro.isNotBlank()) InfoSection("소개", point.intro)
        if (point.forecast.isNotBlank()) InfoSection("예보", point.forecast)
        if (point.ebbf.isNotBlank()) InfoSection("조류", point.ebbf)
        if (point.notice.isNotBlank()) InfoSection("주의사항", point.notice)

        Spacer(Modifier.height(12.dp))

        // 계절별 수온
        InfoSection("수온(봄)", point.wtemp_sp)
        InfoSection("수온(여름)", point.wtemp_su)
        InfoSection("수온(가을)", point.wtemp_fa)
        InfoSection("수온(겨울)", point.wtemp_wi)

        Spacer(Modifier.height(12.dp))

        // 계절별 어종
        InfoSection("어종(봄)", point.fish_sp)
        InfoSection("어종(여름)", point.fish_su)
        InfoSection("어종(가을)", point.fish_fa)
        InfoSection("어종(겨울)", point.fish_wi)

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Center  // 👉 가로 중앙 정렬
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.width(6.dp)) // 👉 가로 간격 좁힘
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
            color = Color(0xFF4FC3F7) // 파란색 포인트
        )
        Text(
            text = if (content.isNotBlank()) content else "정보 없음",
            fontSize = 13.sp,
            color = Color.White,
        )
    }
}
