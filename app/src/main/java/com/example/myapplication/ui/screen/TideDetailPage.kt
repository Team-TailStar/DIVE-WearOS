package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SunMoonEvent(
    val title: String,
    val time: String,
    val color: Color
)

@Composable
fun TideDetailPage(
    date: String = "2025.08.15(금)",
    tideName: String = "4물",
    sunMoonEvents: List<List<SunMoonEvent>> = listOf(
        listOf(
            SunMoonEvent("일출", "05:53", Color(0xFFFFA500)),
            SunMoonEvent("일몰", "19:30", Color(0xFFFF6B6B))
        ),
        listOf(
            SunMoonEvent("월출", "22:37", Color(0xFFFFC107)),
            SunMoonEvent("월몰", "12:17", Color(0xFF1E90FF))
        )
    )
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 날짜 & 물때 표시
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = date,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = tideName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Cyan
                    )
                }
            }

            // 조석 시간 (그냥 글자 배치)
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp), // 🔽 간격 줄이기 (기본 8.dp 이상일 가능성 있음)
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("▼", fontSize = 16.sp, color = Color.Cyan)
                        Text("04:29", fontSize = 16.sp, color = Color.White)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("▲", fontSize = 16.sp, color = Color.Red)
                        Text("09:43", fontSize = 16.sp, color = Color.White)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("▼", fontSize = 16.sp, color = Color.Cyan)
                        Text("16:29", fontSize = 16.sp, color = Color.White)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("▲", fontSize = 16.sp, color = Color.Red)
                        Text("21:43", fontSize = 16.sp, color = Color.White)
                    }
                }
            }

            // ✅ 여기 수정됨
            items(sunMoonEvents.size) { index ->
                val pair = sunMoonEvents[index]
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(6.dp), Arrangement.spacedBy(3.dp)
                    ) {
                        pair.forEach { event ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = event.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = event.color
                                )
                                Text(
                                    text = event.time,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}
