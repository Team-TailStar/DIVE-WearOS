package com.example.dive_app.ui.screen.tide

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dive_app.domain.model.TideInfoData
import androidx.compose.foundation.shape.RoundedCornerShape   // ✅ 추가

@Composable
fun TideDetailSunMoonPage(
    tide: TideInfoData,
    navController: NavController
) {
    var dragSum by remember { mutableStateOf(0f) }
    val trigger = 80f

    val (sunrise, sunset) = tide.pSun.split("/").map { it.trim() }.let {
        (it.getOrNull(0) ?: "-") to (it.getOrNull(1) ?: "-")
    }
    val (moonrise, moonset) = tide.pMoon.split("/").map { it.trim() }.let {
        (it.getOrNull(0) ?: "-") to (it.getOrNull(1) ?: "-")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount -> dragSum += dragAmount },
                    onDragEnd = {
                        if (dragSum >= trigger) {
                            navController.navigate("tide/times") { launchSingleTop = true }
                        }
                        dragSum = 0f
                    },
                    onDragCancel = { dragSum = 0f }
                )
            }
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SunMoonCard(
            title = "일출/일몰",
            label1 = "일출", time1 = sunrise,
            label2 = "일몰", time2 = sunset,
            accent = Color(0xFFFFD54F)
        )
        SunMoonCard(
            title = "월출/월몰",
            label1 = "월출", time1 = moonrise,
            label2 = "월몰", time2 = moonset,
            accent = Color(0xFF80DEEA)
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SunMoonCard(
    title: String,
    label1: String, time1: String,
    label2: String, time2: String,
    accent: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(0.9f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            SunMoonRow(label1, time1)
            Divider(color = Color(0x22FFFFFF))
            SunMoonRow(label2, time2)
        }
    }
}

@Composable
private fun SunMoonRow(label: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFE0B2)
        )
        Text(
            text = time,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
