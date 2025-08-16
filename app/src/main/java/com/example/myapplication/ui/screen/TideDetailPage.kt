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
import com.example.myapplication.domain.model.TideViewModel

data class SunMoonEvent(
    val title: String,
    val time: String,
    val color: Color
)

@Composable
fun TideDetailPage(
    tideViewModel: TideViewModel
) {
    val tideState = tideViewModel.uiState.value

    // 🌞🌙 데이터를 리스트로 변환
    val events = listOf(
        SunMoonEvent("일출/일몰", tideState.sun, Color.Yellow),
        SunMoonEvent("월출/월몰", tideState.moon, Color.Cyan)
    )

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
            // 날짜 & 물때
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = tideState.date,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = tideState.mul,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Cyan
                    )
                }
            }

            // 조석 시간
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("▼", fontSize = 16.sp, color = Color.Cyan)
                        Text(tideState.jowi1, fontSize = 16.sp, color = Color.White)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("▲", fontSize = 16.sp, color = Color.Red)
                        Text(tideState.jowi2, fontSize = 16.sp, color = Color.White)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("▼", fontSize = 16.sp, color = Color.Cyan)
                        Text(tideState.jowi3, fontSize = 16.sp, color = Color.White)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("▲", fontSize = 16.sp, color = Color.Red)
                        Text(tideState.jowi4, fontSize = 16.sp, color = Color.White)
                    }
                }
            }

            // 🌞🌙 일출/일몰, 월출/월몰 카드
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        events.forEach { event ->
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

