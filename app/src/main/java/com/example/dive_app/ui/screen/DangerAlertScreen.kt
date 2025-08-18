package com.example.dive_app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DangerAlertScreen(
    titleText: String = "심박수 위험 감지",
    onCancelClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 타이틀
        Text(
            text = titleText,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 안내 문구
        Text(
            text = "잠시 후 119로 연결됩니다",
            color = Color.Red,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 연결 끊기 버튼
        Button(
            onClick = onCancelClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)), // 초록색
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "연결 끊기",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("연결 끊기", color = Color.White, fontSize = 16.sp)
        }
    }
}
