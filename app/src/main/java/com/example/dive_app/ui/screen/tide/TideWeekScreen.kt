package com.example.dive_app.ui.screen.tide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.dive_app.R
import com.example.dive_app.domain.viewmodel.TideViewModel
import com.example.dive_app.domain.model.TideInfoData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TideWeekScreen(
    navController: NavController,
    tideVM: TideViewModel
) {
    val uiState = tideVM.uiState.value
    val tideList = uiState.tideList.take(7) // 7개만 표시

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 제목
        Text(
            text = "날짜별 물때",
            color = Color(0xFF7CC7F3),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 5.dp)
        )

        // 1행 (3개)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            tideList.take(3).forEach { tide ->
                TideDayCard(tide)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            tideList.drop(3).take(4).forEach { tide ->
                TideDayCard(tide)
            }
        }
    }
}

@Composable
fun TideDayCard(tide: TideInfoData) {
    Card(
        modifier = Modifier
            .width(45.dp)   // 기존 70dp → 60dp
            .height(70.dp), // 기존 90dp → 75dp
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // 날짜
            val dateStr = formatDate(tide.pThisDate)
            Text(
                text = dateStr,
                fontSize = 10.sp,             // 기존 12sp → 10sp
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            // 달 아이콘
            val mulNum = tide.pMul.filter { it.isDigit() }.toIntOrNull() ?: 0
            val moonRes = when (mulNum) {
                1, 2 -> R.drawable.ic_moon_new
                in 3..5 -> R.drawable.ic_moon_crescent
                in 6..7 -> R.drawable.ic_moon_first_quarter
                in 8..10 -> R.drawable.ic_moon_gibbous
                in 11..12 -> R.drawable.ic_moon_full
                in 13..14 -> R.drawable.ic_moon_waning_gibbous
                15 -> R.drawable.ic_moon_last_quarter
                else -> R.drawable.ic_moon_default
            }
            Image(
                painter = painterResource(id = moonRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp) // 기존 26dp → 20dp
            )

            // 물때
            Text(
                text = "${tide.pMul}",
                fontSize = 9.sp,              // 기존 11sp → 9sp
                color = Color(0xFFFFAA33),
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// 날짜 포맷 함수 (요일 제거, MM/dd 형식)
private fun formatDate(dateStr: String): String {
    return try {
        // 예: "2025-8-22-금-6-29"
        val parts = dateStr.split("-")
        if (parts.size >= 3) {
            val month = parts[1]
            val day = parts[2].padStart(2, '0')
            "$month" + "월 " + "$day" + "일"
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}
