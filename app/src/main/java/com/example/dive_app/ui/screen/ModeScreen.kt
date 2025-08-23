package com.example.dive_app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dive_app.domain.viewmodel.AppMode
import com.example.dive_app.domain.viewmodel.AppModeViewModel

@Composable
fun ModeScreen(
    navController: NavController,
    appModeVM: AppModeViewModel,
    onDismissApp: () -> Unit
) {
    // 모드 화면에서 스와이프-백하면 앱 종료
    // 외부에서 SwipeDismissContainer로 감싸줄 거라 버튼만 배치
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101014)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            Text(
                text = "모드 선택",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))

            Button(
                onClick = {
                    appModeVM.setMode(AppMode.FISHING)
                    navController.navigate("tide") {
                        launchSingleTop = true
                        popUpTo("mode") { inclusive = true }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A7FFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text("낚시 모드", fontSize = 16.sp) }

            Button(
                onClick = {
                    appModeVM.setMode(AppMode.NORMAL)
                    navController.navigate("home") {
                        launchSingleTop = true
                        popUpTo("mode") { inclusive = true }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text("일반 모드", fontSize = 16.sp) }

            // 원하면 “앱 종료” 버튼 추가
            // TextButton(onClick = onDismissApp) { Text("나가기", color = Color(0xFF9EA0A6)) }
        }
    }
}
