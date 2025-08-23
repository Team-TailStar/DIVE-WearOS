package com.example.dive_app.ui.screen.tide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dive_app.R
import com.example.dive_app.domain.model.TideInfoData
import com.example.dive_app.domain.viewmodel.TideViewModel

@Composable
fun TideWeekScreen(
    navController: NavController,
    tideVM: TideViewModel,
    showDetailArrows: Boolean = true
) {
    val uiState = tideVM.uiState.value
    val tideList = uiState.tideList.take(7) // 7일치

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ◀ / ▶ 화살표
        if (showDetailArrows) {
            // ◀ 이전: tidewatch 로
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(32.dp)
                    .padding(4.dp)
                    .alpha(0.5f)
                    .offset(x = (-6).dp)
                    .clickable {
                        navController.navigate("tide") {
                            launchSingleTop = true
                            popUpTo("tide") { inclusive = false }
                        }
                    }
            )
            // ▶ 다음: sun/moon 상세로 (기본: 첫 번째 일자)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(32.dp)
                    .padding(4.dp)
                    .alpha(0.5f)
                    .offset(x = (6).dp)
                    .clickable {
                        val first = tideList.firstOrNull()
                        if (first != null) {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedTide", first)
                        }
                        navController.navigate("tide/sunmoon") {
                            launchSingleTop = true
                            popUpTo("tide") { inclusive = false }
                        }
                    }
            )
        }

        // 본문 Grid
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "날짜별 물때",
                color = Color(0xFF7CC7F3),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
            ) {
                tideList.take(3).forEach { tide ->
                    TideDayCard(
                        tide = tide,
                        onClick = {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedTide", tide)
                            navController.navigate("tide/sunmoon") {
                                launchSingleTop = true
                                popUpTo("tide") { inclusive = false }
                            }
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
            ) {
                tideList.drop(3).take(4).forEach { tide ->
                    TideDayCard(
                        tide = tide,
                        onClick = {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedTide", tide)
                            navController.navigate("tide/sunmoon") {
                                launchSingleTop = true
                                popUpTo("tide") { inclusive = false }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TideDayCard(
    tide: TideInfoData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(45.dp)
            .height(70.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            val dateStr = formatDate(tide.pThisDate)
            Text(
                text = dateStr,
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

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
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = tide.pMul,
                fontSize = 9.sp,
                color = Color(0xFFFFAA33),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val parts = dateStr.split("-")
        if (parts.size >= 3) {
            val month = parts[1]
            val day = parts[2].padStart(2, '0')
            "${month}월 ${day}일"
        } else dateStr
    } catch (e: Exception) {
        dateStr
    }
}
