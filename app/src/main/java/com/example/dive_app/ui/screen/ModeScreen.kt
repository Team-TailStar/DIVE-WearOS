package com.example.dive_app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dive_app.domain.viewmodel.AppMode
import com.example.dive_app.domain.viewmodel.AppModeViewModel
import androidx.compose.foundation.layout.BoxWithConstraints

// 머티리얼 아이콘(물방울/설정) 사용
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop

@Composable
fun ModeScreen(
    navController: NavController,
    appModeVM: AppModeViewModel,
    // onDismissApp: () -> Unit   // 쓰지 않으면 주석 or 삭제
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E0E)),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val minPx = minOf(constraints.maxWidth, constraints.maxHeight)
        val cardSideDp = with(density) { (minPx * 0.42f).toDp() }
        val cardCorner = 24.dp
        val horizontalPadding = 20.dp
        val betweenCards = 18.dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Text(
                text = "모드 선택",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(betweenCards),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeCard(
                    title = "낚시모드",
                    icon = Icons.Filled.WaterDrop,   // 물 관련 아이콘으로 대체
                    side = cardSideDp,
                    corner = cardCorner
                ) {
                    appModeVM.setMode(AppMode.FISHING)
                    navController.navigate("home") {
                        launchSingleTop = true
                        popUpTo("mode") { inclusive = true }
                    }
                }

                ModeCard(
                    title = "일반모드",
                    icon = Icons.Filled.Settings,
                    side = cardSideDp,
                    corner = cardCorner
                ) {
                    appModeVM.setMode(AppMode.NORMAL)
                    navController.navigate("home") {
                        launchSingleTop = true
                        popUpTo("mode") { inclusive = true }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    side: Dp,
    corner: Dp,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        onClick = onClick, // ← 이걸로 클릭/리플 처리 끝
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B3B3F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.size(side)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFE0E0E0),
                modifier = Modifier.size(side * 0.38f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                color = Color(0xFFF5F5F7),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
