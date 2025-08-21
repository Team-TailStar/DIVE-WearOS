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
/* ---------- 색 ---------- */
private val Blue   = Color(0xFF3A78FF)   // ▼
private val Red    = Color(0xFFFF6A5F)   // ▲
private val Yellow = Color(0xFFFFC400)
private val Gray   = Color(0xFFBDBDBD)


// "02:00 (642) ▲+469"  또는  "▲ 02:00 642 +469" 같은 변형도 허용
private data class TideRowUi(
    val time: String,       // "HH:mm"
    val height: String,     // "642" (괄호 없이 숫자만)
    val isUp: Boolean,      // ▲ = true, ▼ = false
    val flowSignNum: String // "+469" / "-431" (부호+숫자) 없으면 ""
)

private fun parseLine(rawIn: String): TideRowUi? {
    val raw = rawIn.trim()
    if (raw.isBlank()) return null

    val time = Regex("(\\d{1,2}:\\d{2})").find(raw)?.groupValues?.get(1) ?: return null
    // (642) 또는 642 둘 다 허용
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

private fun formatMul(raw: String) = raw.replace(" ", "") // "4 물" -> "4물"

/* ---------- UI ---------- */
@Composable
fun TideDetailTimesPage(
    tide: TideInfoData,
    navController: NavController
) {
    val listState = rememberLazyListState()
    val rows = remember(tide) {
        // 1순위 pTime1~4, 전부 비어있으면 jowi1~4 사용
        val primary = listOf(tide.pTime1, tide.pTime2, tide.pTime3, tide.pTime4)
            .filter { it.isNotBlank() }

        val source = if (primary.isNotEmpty()) primary
        else listOf(tide.jowi1, tide.jowi2, tide.jowi3, tide.jowi4)

        source.filter { it.isNotBlank() }
            .mapNotNull(::parseLine)
    }

//    LaunchedEffect(rows) {
//        android.util.Log.d("TideTimes", "row count=${rows.size}, rows=$rows")
//    }
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
        // 🔽 0.86f → 0.78f 로 축소 (양옆 여백 ↑)
        val contentWidth = maxWidth * 0.68f

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),         // 항목 간격 조금 줄이기
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
                            modifier = Modifier.size(14.dp)              // 16 → 14
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = formatMul(tide.pMul),
                            color = Yellow,
                            fontSize = 14.sp,                            // 16 → 14
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
                fontSize = 13.sp,                   // 15 → 13
                fontWeight = FontWeight.Medium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = arrow, color = arrowColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold) // 18 → 16
            Spacer(Modifier.width(4.dp))
            Text(text = flow,  color = arrowColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold) // 18 → 16
        }
    }
}

