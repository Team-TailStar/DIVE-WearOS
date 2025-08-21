package com.example.dive_app.ui.screen.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dive_app.domain.viewmodel.HealthViewModel

@Composable
fun HealthScreen(
    viewModel: HealthViewModel
) {
    val records by viewModel.records.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /** ------------------- 심박수 ------------------- **/
        Text("심박수", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))

        val latestRecord = records.lastOrNull()
        if (latestRecord != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "심박수",
                    tint = Color.Red,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("${latestRecord.heartRate} bpm",
                    color = Color.White, fontSize = 22.sp)
            }
        } else {
            Text("데이터 없음", color = Color.Gray)
        }

        Spacer(Modifier.height(6.dp))

        if (records.isNotEmpty()) {
            ChartCard(title = "심박수 기록") { chart ->
                val limited = records.takeLast(30)
                val entries = limited.mapIndexed { index, r ->
                    Entry(index.toFloat(), r.heartRate.toFloat())
                }
                val dataSet = LineDataSet(entries, "심박수").apply {
                    color = android.graphics.Color.CYAN
                    setCircleColor(android.graphics.Color.WHITE)
                    lineWidth = 2f
                    circleRadius = 4f
                    setDrawValues(false)
                }
                chart.data = LineData(dataSet)

                chart.axisLeft.apply {
                    axisMinimum = 40f
                    axisMaximum = 120f
                    gridColor = android.graphics.Color.DKGRAY
                    textColor = android.graphics.Color.WHITE
                }
                chart.xAxis.gridColor = android.graphics.Color.DKGRAY
                chart.animateX(0)
            }
        }

        Spacer(Modifier.height(20.dp))

        /** ------------------- 산소포화도 ------------------- **/
        Text("산소포화도 (SpO₂)", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))

        if (latestRecord != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite, // 필요시 O₂ 아이콘으로 교체
                    contentDescription = "SpO₂",
                    tint = Color.Green,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("${latestRecord.spo2} %",
                    color = Color.White, fontSize = 22.sp)
            }
        } else {
            Text("데이터 없음", color = Color.Gray)
        }

        Spacer(Modifier.height(6.dp))

        if (records.isNotEmpty()) {
            ChartCard(title = "산소포화도 기록") { chart ->
                val limited = records.takeLast(30)
                val entries = limited.mapIndexed { index, r ->
                    Entry(index.toFloat(), r.spo2.toFloat())
                }
                val dataSet = LineDataSet(entries, "SpO₂").apply {
                    color = android.graphics.Color.GREEN
                    setCircleColor(android.graphics.Color.WHITE)
                    lineWidth = 2f
                    circleRadius = 4f
                    setDrawValues(false)
                }
                chart.data = LineData(dataSet)

                chart.axisLeft.apply {
                    axisMinimum = 80f    // 보통 90% 미만은 위험
                    axisMaximum = 100f
                    gridColor = android.graphics.Color.DKGRAY
                    textColor = android.graphics.Color.WHITE
                }
                chart.xAxis.gridColor = android.graphics.Color.DKGRAY
                chart.animateX(0)
            }
        }
    }
}


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
                legend.textColor = android.graphics.Color.WHITE
                legend.textSize = 12f
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
            .height(120.dp)
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}
