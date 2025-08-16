// ui/screen/TideWatchScreen.kt
package com.example.myapplication.ui.screen

// ── Compose / Wear / Foundation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.wear.compose.material.*

// ── Icons / Nav / Time / Math / Coroutines
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin

/* ---------- 데이터 ---------- */

enum class TideType { HIGH, LOW, FLOW }

@Immutable
data class TideMarker(
    val time: LocalTime,
    val type: TideType,
    val color: Color
)

@Immutable
data class TideSegment(
    val start: LocalTime,
    val end: LocalTime,
    val color: Color
)

/* ---------- 화면 루트 ---------- */
@Composable
fun TideWatchScreen(
    navController: NavController? = null,
    date: LocalDate = LocalDate.now(),
    markers: List<TideMarker> = demoMarkers(),
    segments: List<TideSegment> = demoSegments(),
) {
    var currentDate by remember { mutableStateOf(date) }

    var centerTime by remember {
        mutableStateOf(
            ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .toLocalTime()
                .withSecond(0)
                .withNano(0)
        )
    }

    // 오버레이(설명) 표시 상태
    var showLegend by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            centerTime = LocalTime.now()
            delay(1000L)
        }
    }

    SwipeToDismissBox(onDismissed = { navController?.popBackStack() }) {
        Scaffold(
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
        ) {
            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                // .onRotaryScrollEvent { true } // 필요 시 재활성화
            ) {
                val minSide = if (maxWidth < maxHeight) maxWidth else maxHeight
                val dial = minSide * 0.54f
                val ringRadius = dial / 2
                val outerLabelRadius = ringRadius + 8.dp

                Box(Modifier.fillMaxSize()) {

                    // 좌/우 화살표(날짜 이동)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "prev day",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                            .size(18.dp)
                            .clickable { currentDate = currentDate.minusDays(1) }
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "next day",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .size(18.dp)
                            .clickable { currentDate = currentDate.plusDays(1) }
                    )

                    // 다이얼
                    TideDial(
                        diameter = dial,
                        centerTime = centerTime,
                        date = currentDate,
                        segments = segments,
                        markers = markers,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // (선택) 바깥 라벨 - 필요 없으면 주석처리
                    SideCallouts(
                        items = demoCallouts(),
                        radius = outerLabelRadius,
                        modifier = Modifier.align(Alignment.Center),
                        labelPadDp = 4.dp,
                        nudgeDp = 2.dp
                    )

                    // 맨 위 중앙의 느낌표 아이콘 (라벨 설명 오버레이)
                    InfoCircle(
                        onClick = { showLegend = true },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )

                    // 오버레이(설명창)
                    if (showLegend) {
                        LegendOverlay(
                            onDismiss = { showLegend = false },
                            items = listOf(
                                LegendItem("만조", Color(0xFF1E88E5), "만조 시각/구간 표시"),
                                LegendItem("간조", Color(0xFFE53935), "간조 시각/구간 표시"),
                                LegendItem("일출·일몰", Color(0xFFB0BEC5), "일출/일몰 관련 표시"),
                                LegendItem("월출·월몰", Color(0xFF9FA8DA), "월출/월몰 관련 표시"),
                            )
                        )
                    }
                }
            }
        }
    }
}

/* ---------- 다이얼 ---------- */

@Composable
private fun TideDial(
    diameter: Dp,
    centerTime: LocalTime,
    date: LocalDate,
    segments: List<TideSegment>,
    markers: List<TideMarker>,
    modifier: Modifier = Modifier
) {
    val ringW = 12f
    val tickW = 2f

    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rOuter = size.minDimension / 2f
            val rRing = rOuter - ringW

            // 24시간 눈금
            repeat(24) { h ->
                val a = hourToAngleRad(h.toFloat())
                val r1 = rRing - 14f
                val r2 = rRing + 14f
                val x1 = cx + r1 * cos(a)
                val y1 = cy + r1 * sin(a)
                val x2 = cx + r2 * cos(a)
                val y2 = cy + r2 * sin(a)
                drawLine(
                    color = if (h % 3 == 0) Color(0xFF7A7A7A) else Color(0xFF3A3A3A),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = if (h % 3 == 0) 3f else tickW
                )
            }

            // 기본 링
            drawArc(
                color = Color(0xFF545454),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = ringW, cap = StrokeCap.Butt)
            )

            // 강조 구간
            segments.forEach { s ->
                val start = radToDeg(timeToAngleRad(s.start))
                val sweep = sweepDegrees(s.start, s.end)
                drawArc(
                    color = s.color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = ringW, cap = StrokeCap.Butt)
                )
            }

            // 포인트 점
            markers.forEach { m ->
                val a = timeToAngleRad(m.time)
                val px = cx + rRing * cos(a)
                val py = cy + rRing * sin(a)
                drawCircle(
                    color = m.color,
                    radius = 5f,
                    center = Offset(px, py)
                )
            }
        }

        // 중앙 날짜 + 현재 시간
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%d.%d.%d".format(date.year, date.monthValue, date.dayOfMonth),
                color = Color.White,
                style = MaterialTheme.typography.caption1,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "%d:%02d".format(centerTime.hour, centerTime.minute),
                color = Color.White,
                style = MaterialTheme.typography.title2,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* ---------- 바깥 라벨 (시간 작은 표기) ---------- */

@Composable
private fun SideCallouts(
    items: List<Pair<LocalTime, Color>>,
    radius: Dp,
    modifier: Modifier = Modifier,
    labelPadDp: Dp = 4.dp,
    nudgeDp: Dp = 2.dp
) {
    val rLabel = radius + labelPadDp
    val density = LocalDensity.current
    val nudgePx = with(density) { nudgeDp.toPx() }

    Box(modifier = modifier.size(rLabel * 2).padding(8.dp)) {
        items.forEachIndexed { index, (time, color) ->
            val a = timeToAngleRad(time)
            val cosA = cos(a)
            val sinA = sin(a)
            val x = (rLabel.value * cosA).dp
            val y = (rLabel.value * sinA).dp
            val prefix = if (index % 2 == 0) "▲" else "▼"

            Box(
                modifier = Modifier
                    .offset(x, y)
                    .graphicsLayer {
                        translationX += nudgePx * cosA
                        translationY += nudgePx * sinA
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$prefix ${time.toString().substring(0, 5)}",
                    color = color,
                    fontSize = 6.sp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.caption2,
                    fontWeight = FontWeight.Light,
                    maxLines = 1
                )
            }
        }
    }
}

/* ---------- 상단 느낌표 아이콘 ---------- */

@Composable
private fun InfoCircle(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(Color(0xFF424242), shape = RoundedCornerShape(50))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "!",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ---------- 오버레이(설명창) ---------- */

@Immutable
data class LegendItem(val title: String, val color: Color, val desc: String)

@Composable
private fun LegendOverlay(
    onDismiss: () -> Unit,
    items: List<LegendItem>,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() } // 바깥 탭으로 닫기
    ) {
        Card(
            onClick = {}, // 카드 탭은 무시
            backgroundPainter = CardDefaults.cardBackgroundPainter(
                startBackgroundColor = Color(0xFF2B2B2B),
                endBackgroundColor = Color(0xFF2B2B2B)
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 12.dp)
        ) {
            Column(
                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "표시 설명",
                    color = Color.White,
                    style = MaterialTheme.typography.title3,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                items.forEach { itx ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .background(itx.color, shape = RoundedCornerShape(50))
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                itx.title,
                                color = Color.White,
                                style = MaterialTheme.typography.caption1,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                itx.desc,
                                color = Color(0xFFBDBDBD),
                                style = MaterialTheme.typography.caption2
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("닫기", color = Color.Black)
                }
            }
        }
    }
}

/* ---------- 유틸 ---------- */

private fun hourToAngleRad(hour: Float): Float {
    val deg = (hour / 24f) * 360f - 90f
    return Math.toRadians(deg.toDouble()).toFloat()
}
private fun timeToAngleRad(t: LocalTime): Float {
    val sec = t.toSecondOfDay().toFloat()
    val deg = (sec / 86400f) * 360f - 90f
    return Math.toRadians(deg.toDouble()).toFloat()
}
private fun radToDeg(rad: Float) = Math.toDegrees(rad.toDouble()).toFloat()
private fun sweepDegrees(start: LocalTime, end: LocalTime): Float {
    val s = start.toSecondOfDay()
    val e = end.toSecondOfDay()
    val delta = if (e >= s) e - s else 86400 - s + e
    return delta / 86400f * 360f
}

/* ---------- 데모 데이터 ---------- */

private fun demoMarkers() = listOf(
    TideMarker(LocalTime.of(1, 40), TideType.HIGH, Color(0xFF1E88E5)),
    TideMarker(LocalTime.of(7, 9),  TideType.LOW,  Color(0xFFE53935)),
    TideMarker(LocalTime.of(14, 0), TideType.HIGH, Color(0xFF1E88E5)),
    TideMarker(LocalTime.of(19, 30),TideType.LOW,  Color(0xFFE53935)),
)

private fun demoSegments() = listOf(
    TideSegment(LocalTime.of(0, 50),  LocalTime.of(2, 10), Color(0xFF4FC3F7)),
    TideSegment(LocalTime.of(13, 30), LocalTime.of(14, 40), Color(0xFF4FC3F7)),
    TideSegment(LocalTime.of(6, 30),  LocalTime.of(7, 30), Color(0xFFEF5350)),
    TideSegment(LocalTime.of(18, 50), LocalTime.of(19, 40), Color(0xFFEF5350)),
)

private fun demoCallouts() = listOf(
    LocalTime.of(1, 40) to Color(0xFF1E88E5),
    LocalTime.of(7,  9) to Color(0xFFE53935),
    LocalTime.of(14, 0) to Color(0xFF1E88E5),
    LocalTime.of(19, 30) to Color(0xFFE53935),
    LocalTime.of(5, 51) to Color(0xFFB0BEC5),
    LocalTime.of(19, 0) to Color(0xFFB0BEC5),
    LocalTime.of(7, 32) to Color(0xFF9FA8DA),
    LocalTime.of(19, 59) to Color(0xFF9FA8DA),
)
