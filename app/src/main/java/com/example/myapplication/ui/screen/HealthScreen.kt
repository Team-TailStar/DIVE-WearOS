package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HealthScreen() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 혈압 섹션
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.DarkGray)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("혈압", color = Color.White, fontSize = 10.sp)
        }

        Spacer(Modifier.height(6.dp))
        Row {
            Text("수축기", color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.width(16.dp))
            Text("이완기", color = Color.Gray, fontSize = 14.sp)
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(Modifier.width(8.dp))
            Text("120", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text("/", color = Color.Gray, fontSize = 24.sp)
            Spacer(Modifier.width(8.dp))
            Text("70", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))

        }

        Spacer(Modifier.height(10.dp))
        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(10.dp))

        // 심박수 섹션
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.DarkGray)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("심박수", color = Color.White, fontSize = 10.sp)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "심박수",
                tint = Color.Red,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("120 bpm", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // ---------------------------
        // 2️⃣ 건강 기록 (그래프 영역)
        // ---------------------------
        Spacer(Modifier.height(40.dp))

        Text(
            text = "내 건강 기록",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        // 그래프 박스 (임시 Placeholder)
        Box(
            modifier = Modifier
                .width(170.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            Text("📊 그래프 표시 영역", color = Color.White, fontSize = 14.sp)
        }

        Spacer(Modifier.height(30.dp))
    }
}