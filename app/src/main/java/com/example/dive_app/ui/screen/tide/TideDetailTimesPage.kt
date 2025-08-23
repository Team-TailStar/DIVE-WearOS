package com.example.dive_app.ui.screen.tide

import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.clickable
import androidx.compose.ui.zIndex

/* ---------- 색 ---------- */
private val Blue   = Color(0xFF3A78FF)   // ▼
private val Red    = Color(0xFFFF6A5F)   // ▲
private val Yellow = Color(0xFFFFC400)
private val Gray   = Color(0xFFBDBDBD)

private data class TideRowUi(
    val time: String,       // "HH:mm"
    val height: String,     // "642"
    val isUp: Boolean,      // ▲ = true, ▼ = false
    val flowSignNum: String // "+469" / "-431" (없으면 "")
)

private fun parseLine(rawIn: String): TideRowUi? {
    val raw = rawIn.trim()
    if (raw.isBlank()) return null

    val time = Regex("(\\d{1,2}:\\d{2})").find(raw)?.groupValues?.get(1) ?: return null
    val height = Regex("\\((\\d+)\\)").find(raw)?.groupValues?.get(1)
        ?: Regex("\\b(\\d{2,4})\\b").findAll(raw).map { it.value }
            .firstOrNull { it.length in 2..4 } ?: "--"

    val isUp = raw.contains('▲')
    val isDown = raw.contains('▼')
    val flow = Regex("([+\\-]\\d+)").find(raw)?.groupValues?.get(1) ?: ""

    return TideRowUi(
        time = time,
        height = height,
        isUp = if (isUp) true else if (isDown) false else false,
        flowSignNum = flow
    )
}

private fun formatDate(pThisDate: String): String {
    // "2025-8-21-목-6-28" -> "2025.08.21(목)"
    val parts = pThisDate.split("-")
    val y = parts.getOrNull(0) ?: ""
    val m = parts.getOrNull(1)?.padStart(2, '0') ?: ""
    val d = parts.getOrNull(2)?.padStart(2, '0') ?: ""
    val dow = parts.getOrNull(3) ?: ""
    return "$y.$m.$d($dow)"
}

private fun formatMul(raw: String) = raw.replace(" ", "")

/* ---------- UI ---------- */
@Composable
fun TideDetailTimesPage(
    tide: TideInfoData,
    navController: NavController,
    showDetailArrows: Boolean = true      // ✅ 추가: 낚시모드에서 화살표 숨김 제어
) {
    val listState = rememberLazyListState()
    val rows = remember(tide) {
        val primary = listOf(tide.pTime1, tide.pTime2, tide.pTime3, tide.pTime4)
            .filter { it.isNotBlank() }
        val source = if (primary.isNotEmpty()) primary
        else listOf(tide.jowi1, tide.jowi2, tide.jowi3, tide.jowi4)

        source.filter { it.isNotBlank() }.mapNotNull(::parseLine)
    }

    val density = LocalDensity.current
    val triggerPx = with(density) { 56.dp.toPx() } // 맨 위에서 위로 당김 임계치
    var dragAccum by remember { mutableStateOf(0f) }

    // LazyColumn이 먹기 전에 '맨 위에서 위로 끌면' 전환
    val nestedScrollConnection = remember(listState, tide) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y // 위로 음수
                val atTop = listState.firstVisibleItemIndex == 0 &&
                        listState.firstVisibleItemScrollOffset == 0
                if (!atTop) {
                    dragAccum = 0f
                    return Offset.Zero
                }
                if (dy < 0f) {
                    dragAccum += dy
                    if (dragAccum <= -triggerPx) {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedTide", tide)
                        navController.navigate("tide/sunmoon") { launchSingleTop = true }
                        dragAccum = 0f
                    }
                } else {
                    dragAccum = 0f
                }
                return Offset.Zero
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .nestedScroll(nestedScrollConnection)
            .padding(vertical = 10.dp)
    ) {
        val contentWidth = maxWidth * 0.68f

        // ◀/▶ 화살표는 플래그로 노출 제어
        if (showDetailArrows) {
            // ◀ 왼쪽 버튼 (sunmoon으로)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .padding(8.dp)
                    .alpha(0.5f)
                    .zIndex(10f)
                    .offset(x = (-8).dp)
                    .clickable {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedTide", tide)

                        navController.navigate("tide/sunmoon") {
                            launchSingleTop = true
                            popUpTo("tide") { inclusive = false }
                        }
                    }
            )

            // ▶ 오른쪽 버튼 (tide 메인으로)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                    .padding(8.dp)
                    .alpha(0.5f)
                    .zIndex(10f)
                    .offset(x = (8).dp)
                    .clickable {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedTide", tide)

                        navController.navigate("tide") {
                            launchSingleTop = true
                            popUpTo("tide") { inclusive = false }
                        }
                    }
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                top = 20.dp,
                bottom = 12.dp,
                start = (maxWidth - contentWidth) / 2,
                end = (maxWidth - contentWidth) / 2
            )
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatDate(tide.pThisDate),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = null,
                            tint = Yellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = formatMul(tide.pMul),
                            color = Yellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            items(rows) { r ->
                TideTimeRow(
                    time = r.time,
                    height = r.height,
                    isUp = r.isUp,
                    flow = r.flowSignNum,
                    rowWidth = contentWidth
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun TideTimeRow(
    time: String,
    height: String,
    isUp: Boolean,
    flow: String,
    rowWidth: Dp
) {
    val arrow = if (isUp) "▲" else "▼"
    val arrowColor = if (isUp) Red else Blue

    Row(
        modifier = Modifier.width(rowWidth),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = time,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "($height)",
                color = Gray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = arrow, color = arrowColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(4.dp))
            Text(text = flow,  color = arrowColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
