package com.example.dive_app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.example.dive_app.domain.model.HealthRecord
import com.example.dive_app.domain.viewmodel.HealthViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun HealthScreen(
    viewModel: HealthViewModel,
    showArrows: Boolean = true,
    autoCycle: Boolean = false,
    intervalMillis: Long = 3000L
) {
    val records by viewModel.records.collectAsState()
    val latest = remember(records) { records.lastOrNull() }

    var page by remember { mutableStateOf(0) }      // 0 개요, 1 심박, 2 SpO2
    var paused by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(autoCycle, intervalMillis, paused) {
        if (!autoCycle) return@LaunchedEffect
        while (isActive) {
            delay(intervalMillis)
            if (!paused) page = (page + 1) % 3
        }
    }
    LaunchedEffect(toast) { if (toast != null) { delay(800); toast = null } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // 본문
        when (page) {
            0 -> OverviewPage(latest)

            1 -> HistoryPage(
                records = records,
                type = HistoryType.HEART,
                onSelectType = { sel -> page = if (sel == HistoryType.HEART) 1 else 2 }
            )

            2 -> HistoryPage(
                records = records,
                type = HistoryType.SPO2,
                onSelectType = { sel -> page = if (sel == HistoryType.HEART) 1 else 2 }
            )
        }


        if (showArrows) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-12).dp)
                    .size(28.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable { page = (page + 2) % 3 }   // ← 0↔1↔2 순환(왼쪽)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 12.dp)
                    .size(28.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable { page = (page + 1) % 3 }   // ← 0→1→2→0 순환(오른쪽)
            )
        }

        // ⏸/▶ 토스트
        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = toast ?: "", color = Color.White, fontSize = 28.sp)
        }

        // 🔥 터치 오버레이: AndroidView/클릭 위에 깔아서 항상 탭 감지
        if (autoCycle) {
            Box(
                modifier = Modifier
                    .matchParentSize()           // 화면 전체 덮기
                    .pointerInput(Unit) {
                        detectTapGestures {
                            paused = !paused
                            toast = if (paused) "⏸" else "▶"
                        }
                    }
            )
        }
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

private enum class MetricStatus { Normal, Danger, None }

@Composable
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
 *  Page 2/3: 기록(그래프)
 * =========================== */

private enum class HistoryType { HEART, SPO2 }

@Composable
private fun HistoryPage(
    records: List<HealthRecord>,
    type: HistoryType,
    onSelectType: (HistoryType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 상단 토글(다른 쪽을 누르면 해당 페이지로 이동)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            ToggleChip(
                text = "심박수",
                selected = type == HistoryType.HEART
            ) { onSelectType(HistoryType.HEART) }
            Spacer(Modifier.width(4.dp))
            ToggleChip(
                text = "산소포화도",
                selected = type == HistoryType.SPO2
            ) { onSelectType(HistoryType.SPO2) }
        }

        ChartCard(title = if (type == HistoryType.HEART) "심박수 기록" else "산소포화도 기록") { chart ->
            val limited = records.takeLast(30)
            val entries = if (type == HistoryType.HEART)
                limited.mapIndexed { idx, r -> Entry(idx.toFloat(), r.heartRate.toFloat()) }
            else
                limited.mapIndexed { idx, r -> Entry(idx.toFloat(), r.spo2.toFloat()) }

            val dataSet = LineDataSet(entries, if (type == HistoryType.HEART) "심박수" else "SpO₂").apply {
                color = if (type == HistoryType.HEART) android.graphics.Color.CYAN else android.graphics.Color.GREEN
                setCircleColor(android.graphics.Color.WHITE)
                lineWidth = 2f
                circleRadius = 3f
                setDrawValues(false)
            }

            chart.data = LineData(dataSet)
            chart.axisLeft.apply {
                if (type == HistoryType.HEART) { axisMinimum = 40f; axisMaximum = 120f }
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
            .clip(RoundedCornerShape(50))
            .background(if (selected) Color(0xFF1976D2) else Color(0xFF2B2B2B))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
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
            .height(96.dp)
            .width(146.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF232323))
            .padding(4.dp)
    )
}
