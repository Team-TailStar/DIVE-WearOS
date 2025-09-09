package com.example.dive_app.ui.screen

import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.min
import com.example.dive_app.R
import com.example.dive_app.domain.viewmodel.WeatherViewModel
import com.example.dive_app.MainActivity
import kotlin.math.roundToInt
import java.text.DecimalFormat

@Composable
fun SeaWeatherScreen(
    navController: androidx.navigation.NavController,
    weatherViewModel: WeatherViewModel,
    showDetailArrows: Boolean = true    // ✅ 낚시모드에서 화살표 숨김 제어
) {
    val context = LocalContext.current
    val uiState by weatherViewModel.uiState
    LaunchedEffect(Unit) { (context as MainActivity).requestWeather() }

    val waterTempText = formatTempC(uiState.obsWt)
    val waveHeightText = formatWaveM(uiState.waveHt)
    val waveDirText = translateDir(uiState.waveDir)

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val base = min(maxWidth, maxHeight)

        val pillSize = baseToSp(base, 0.038f)
        val tempSize = baseToSp(base, 0.115f)
        val waveSize = baseToSp(base, 0.115f)
        val dirSize  = baseToSp(base, 0.042f)

        Image(
            painter = painterResource(R.drawable.sea_background2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ◀/▶ 아이콘은 플래그로 노출 제어
        if (showDetailArrows) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("air_quality") {
                            launchSingleTop = true
                            popUpTo("sea_weather") { inclusive = false } // ✅ 라우트명 통일
                        }
                    }
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("weather") {
                            launchSingleTop = true
                            popUpTo("sea_weather") { inclusive = false } // ✅ 라우트명 통일
                        }
                    }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── 수온: 라벨은 왼쪽 위, 값은 가운데
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Pill("수온", pillSize, modifier = Modifier.align(Alignment.Start))
                Text(
                    text = waterTempText,
                    fontSize = tempSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(Modifier.height(base * 0.02f))

            // ── 파고/파향: 라벨은 왼쪽 위, 값은 가운데
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Pill("파고/파향", pillSize, modifier = Modifier.align(Alignment.Start))
                Text(
                    text = waveHeightText,
                    fontSize = waveSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(Modifier.height(1.dp))

            // ── 파향
            Text(
                text = waveDirText,
                fontSize = dirSize,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101010)
            )
        }

        // 출처 표기(사진)
        Text(
            text = "배경 이미지: derich, Freepik",
            fontSize = 5.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

/* ───── 컴포넌트 ───── */
@Composable
private fun Pill(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xAA2E3A46), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = fontSize, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

/* ───── 포맷/파싱 ───── */
private fun formatTempC(raw: String?): String {
    val v = parseNumber(raw) ?: return "-"
    return "${v.roundToInt()}°C"
}
private fun formatWaveM(raw: String?): String {
    val v = parseNumber(raw) ?: return "-"
    return DecimalFormat("#0.0").format(v) + " m"
}
private fun parseNumber(raw: String?): Double? {
    if (raw.isNullOrBlank()) return null
    val cleaned = raw.trim().filter { it.isDigit() || it == '.' || it == '-' }
    return cleaned.toDoubleOrNull()
}

/* ───── DP 비율 → sp ───── */
@Composable
private fun baseToSp(base: Dp, ratio: Float): TextUnit {
    val density = LocalDensity.current
    val px = with(density) { base.toPx() * ratio }
    val spValue = px / (density.density * density.fontScale)
    return spValue.sp
}

// 영어 방향 → 한글 변환 (중간점 포함)
private fun translateDir(dir: String?): String = when (dir?.uppercase()) {
    "N"   -> "북"
    "NNE" -> "북북동"
    "NE"  -> "북동"
    "ENE" -> "동북동"
    "E"   -> "동"
    "ESE" -> "동남동"
    "SE"  -> "남동"
    "SSE" -> "남남동"
    "S"   -> "남"
    "SSW" -> "남남서"
    "SW"  -> "남서"
    "WSW" -> "서남서"
    "W"   -> "서"
    "WNW" -> "서북서"
    "NW"  -> "북서"
    "NNW" -> "북북서"
    else  -> dir ?: "-"
}
