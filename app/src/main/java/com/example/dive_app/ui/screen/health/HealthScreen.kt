package com.example.dive_app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dive_app.domain.model.HealthRecord
import com.example.dive_app.domain.viewmodel.HealthViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun HealthScreen(
    viewModel: HealthViewModel
) {
    val records by viewModel.records.collectAsState()
    val latest = remember(records) { records.lastOrNull() }

    // 0: 개요 / 1: 기록 그래프
    var page by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        when (page) {
            0 -> OverviewPage(latest)
            1 -> HistoryPage(records)
        }

        // ←
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "이전",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-12).dp)   // 👈 왼쪽으로 더 빼기
                .size(28.dp)
                .clip(RoundedCornerShape(50))
                .clickable { page = if (page == 0) 1 else page - 1 }
        )

// →
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "다음",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 12.dp)     // 👈 오른쪽으로 더 빼기
                .size(28.dp)
                .clip(RoundedCornerShape(50))
                .clickable { page = if (page == 1) 0 else page + 1 }
        )

    }
}

/* ===========================
 *  Page 1: 개요(카드 2개)
 * =========================== */
@Composable
private fun OverviewPage(latest: HealthRecord?) {
    val MIN_BPM = 40
    val MIN_SPO2 = 90

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetricColumn(
            label = "심박수",
            value = latest?.heartRate,
            unit = "bpm",
            iconTint = Color.Red,
            status = latest?.heartRate?.let { v ->
                if (v <= 0) MetricStatus.None
                else if (v < MIN_BPM) MetricStatus.Danger
                else MetricStatus.Normal
            } ?: MetricStatus.None
        )
        MetricColumn(
            label = "산소포화도",
            value = latest?.spo2,
            unit = "%",
            iconTint = Color(0xFF00E676),
            status = latest?.spo2?.let { v ->
                if (v <= 0) MetricStatus.None
                else if (v < MIN_SPO2) MetricStatus.Danger
                else MetricStatus.Normal
            } ?: MetricStatus.None
        )
    }
}
    // 내부 선언 그대로 유지해도 됨(권장은 바깥으로 이동)
private enum class MetricStatus { Normal, Danger, None }

@Composable // 👈 중복이던 두 번째 @Composable 삭제
private fun MetricColumn(
    label: String,
    value: Int?,
    unit: String,
    iconTint: Color,
    status: MetricStatus
) {
    val display = if (value != null && value > 0) value.toString() else "--"
    val (chipText, chipColor) = when (status) {
        MetricStatus.Normal -> "정상" to Color(0xFF2E7D32)
        MetricStatus.Danger -> "위험" to Color(0xFFD32F2F)
        MetricStatus.None   -> "데이터 없음" to Color(0xFF616161)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Text(display, color = Color.White, fontSize = 22.sp)
        Text(unit, color = Color(0xFF9E9E9E), fontSize = 11.sp)
        Text(label, color = Color.White, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(chipColor)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(chipText, color = Color.White, fontSize = 10.sp)
        }
    }
}


/* ===========================
 *  Page 2: 기록(그래프 + 토글)
 * =========================== */

@Composable
private fun HistoryPage(records: List<HealthRecord>) {
    var tab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp),   // ✅ 좌우 여백만
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center   // ✅ 중앙 정렬
    ) {
        // 버튼 Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, bottom = 8.dp), // ✅ 왼쪽에서 20dp 띄우기
            horizontalArrangement = Arrangement.Start
        ) {
            ToggleChip(text = "심박수", selected = tab == 0) { tab = 0 }
            Spacer(Modifier.width(4.dp))
            ToggleChip(text = "산소포화도", selected = tab == 1) { tab = 1 }
        }


        // 그래프
        ChartCard(title = if (tab == 0) "심박수 기록" else "산소포화도 기록") { chart ->
            val limited = records.takeLast(30)
            val entries = if (tab == 0)
                limited.mapIndexed { idx, r -> Entry(idx.toFloat(), r.heartRate.toFloat()) }
            else
                limited.mapIndexed { idx, r -> Entry(idx.toFloat(), r.spo2.toFloat()) }

            val dataSet = LineDataSet(entries, if (tab == 0) "심박수" else "SpO₂").apply {
                color = if (tab == 0) android.graphics.Color.CYAN else android.graphics.Color.GREEN
                setCircleColor(android.graphics.Color.WHITE)
                lineWidth = 2f
                circleRadius = 3f
                setDrawValues(false)
            }

            chart.data = LineData(dataSet)
            chart.axisLeft.apply {
                if (tab == 0) { axisMinimum = 40f; axisMaximum = 120f }
                else { axisMinimum = 80f; axisMaximum = 100f }
                gridColor = android.graphics.Color.DKGRAY
                textColor = android.graphics.Color.WHITE
                textSize = 4f
            }
            chart.xAxis.apply {
                gridColor = android.graphics.Color.DKGRAY
                textColor = android.graphics.Color.WHITE
                textSize = 8f
            }
            chart.description = Description().apply { text = "" }
            chart.axisRight.isEnabled = false
            chart.legend.isEnabled = false
            chart.setExtraOffsets(4f, 4f, 4f, 4f)
            chart.animateX(0)
        }
    }
}



@Composable
private fun ToggleChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))            // ⬅ 완전 알약
            .background(if (selected) Color(0xFF1976D2) else Color(0xFF2B2B2B))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp) // 크기 소폭 축소
    ) {
        Text(
            text,
            color = if (selected) Color.White else Color(0xFFBBBBBB),
            fontSize = 10.sp
        )
    }
}


/* 공용: MPAndroidChart 카드 */
@Composable
fun ChartCard(title: String, updateChart: (LineChart) -> Unit) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description = Description().apply { text = title }
                setTouchEnabled(true)
                setPinchZoom(true)
                axisLeft.textColor = android.graphics.Color.WHITE
                axisRight.isEnabled = false
                xAxis.textColor = android.graphics.Color.WHITE
                legend.isEnabled = false
                setNoDataText("데이터 없음")
                setNoDataTextColor(android.graphics.Color.LTGRAY)
            }
        },
        update = { chart ->
            chart.clear()
            updateChart(chart)
            chart.invalidate()
        },
        modifier = Modifier
            .height(96.dp)               // 110dp → 96dp
            .width(146.dp)               // 160dp → 146dp
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF232323))
            .padding(4.dp)
    )
}
