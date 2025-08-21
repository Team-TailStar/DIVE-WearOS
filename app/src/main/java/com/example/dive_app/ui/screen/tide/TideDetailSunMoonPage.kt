package com.example.dive_app.ui.screen.tide
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dive_app.domain.model.TideInfoData

@Composable
fun TideDetailSunMoonPage(
    tide: TideInfoData,
    navController: NavController
) {
    // 🔸 pSun / pMoon "HH:mm/HH:mm" 파싱
    val (sunrise, sunset) = remember(tide.pSun) {
        val p = tide.pSun.split("/").map { it.trim() }
        (p.getOrNull(0) ?: "-") to (p.getOrNull(1) ?: "-")
    }
    val (moonrise, moonset) = remember(tide.pMoon) {
        val p = tide.pMoon.split("/").map { it.trim() }
        (p.getOrNull(0) ?: "-") to (p.getOrNull(1) ?: "-")
    }


    var dragDown by remember { mutableStateOf(0f) }
    val trigger = with(LocalDensity.current) { 56.dp.toPx() }
    val nested = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy > 0f) {
                    dragDown += dy
                    if (dragDown >= trigger) {
                        navController.popBackStack("tide", inclusive = false)
                        dragDown = 0f
                    }
                } else if (dy < 0f) {
                    dragDown = 0f
                }
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .nestedScroll(nested)
            .padding(vertical = 20.dp),   // 위아래 패딩도 살짝 줄임
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically), // 🔹 카드 사이 간격 줄임
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SunMoonCard(
            label1 = "일출", time1 = sunrise,
            label2 = "일몰", time2 = sunset,
            accent = Color(0xFFFFB300)
        )
        SunMoonCard(
            label1 = "월출", time1 = moonrise,
            label2 = "월몰", time2 = moonset,
            accent = Color(0xFF90CAF9)
        )
    }

}
@Composable
private fun SunMoonCard(
    label1: String,
    time1: String,
    label2: String,
    time2: String,
    accent: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.70f)   // 🔹 폭 줄임 (0.85 → 0.78 정도)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF2C2C2C))
            .padding(horizontal = 12.dp, vertical = 8.dp) // 🔹 내부 여백 줄임
    ) {
        SunMoonRow(label1, time1, accent)
        Spacer(modifier = Modifier.height(4.dp)) // 🔹 행 사이 간격 줄임
        SunMoonRow(label2, time2, accent)
    }
}
@Composable
private fun SunMoonRow(
    label: String,
    time: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.86f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            modifier = Modifier.padding(start = 11.dp)   // ← 글자 오른쪽 이동
        )
        Text(
            text = time,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}


