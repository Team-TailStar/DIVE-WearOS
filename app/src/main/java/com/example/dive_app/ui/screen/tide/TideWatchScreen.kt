package com.example.dive_app.ui.screen
import androidx.compose.material.icons.filled.WbSunny   // 해 아이콘
import androidx.compose.material.icons.filled.DarkMode // 달 아이콘
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape

// ── Compose / Wear / Foundation
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.wear.compose.material.*

// ── Icons / Nav / Time / Math / Coroutines
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavController
import com.example.dive_app.domain.model.TideInfoData
import com.example.dive_app.domain.viewmodel.TideViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalContext
import com.example.dive_app.MainActivity

import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import java.util.regex.Pattern
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.filled.WbSunny   // 해 아이콘
import androidx.compose.material.icons.filled.DarkMode // 달 아이콘

/* ---------- 유틸/포맷 ---------- */

private val TIME_REGEX: Pattern = Pattern.compile("(\\d{1,2}:\\d{2})")
private val FLEX_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")

fun parsePThisDate(raw: String): LocalDate? {
    val p = raw.split("-")
    return runCatching { LocalDate.of(p[0].toInt(), p[1].toInt(), p[2].toInt()) }.getOrNull()
}


/* ---------- 데이터 ---------- */

enum class TideType { HIGH, LOW, FLOW }

@Immutable
data class CalloutItem(
    val time: LocalTime,
    val text: String,
    val color: Color
)

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
// ── SUN / MOON 파싱 & 파생들 ───────────────────────────────────────────────
private fun parseRiseSet(raw: String): Pair<LocalTime?, LocalTime?> {
    if (raw.isBlank() || !raw.contains("/")) return null to null
    val p = raw.split("/")
    val rise = runCatching { LocalTime.parse(p.getOrNull(0)?.trim(), FLEX_FMT) }.getOrNull()
    val set  = runCatching { LocalTime.parse(p.getOrNull(1)?.trim(), FLEX_FMT) }.getOrNull()
    return rise to set
}

fun TideInfoData.sunTimes(): Pair<LocalTime?, LocalTime?> = parseRiseSet(pSun)
fun TideInfoData.moonTimes(): Pair<LocalTime?, LocalTime?> = parseRiseSet(pMoon)

/** 일/월 출몰의 ‘라벨’ 아이템 (예: "일출 05:56") */
fun TideInfoData.toSunMoonCallouts(): List<CalloutItem> {
    val (sr, ss) = sunTimes()
    val (mr, ms) = moonTimes()
    val sunColor  = Color(0xFFB0BEC5) // 일출·일몰 (기존 레전드 색)
    val moonColor = Color(0xFF9FA8DA) // 월출·월몰

    return buildList {
        sr?.let { add(CalloutItem(it, "일출 ${FLEX_FMT.format(it)}", sunColor)) }
        ss?.let { add(CalloutItem(it, "일몰 ${FLEX_FMT.format(it)}", sunColor)) }
        mr?.let { add(CalloutItem(it, "월출 ${FLEX_FMT.format(it)}", moonColor)) }
        ms?.let { add(CalloutItem(it, "월몰 ${FLEX_FMT.format(it)}", moonColor)) }
    }
}

/** 출몰 시각에 ‘점’ 마커 찍기 */
fun TideInfoData.toSunMoonMarkers(): List<TideMarker> {
    val (sr, ss) = sunTimes()
    val (mr, ms) = moonTimes()
    return buildList {
        val sunColor  = Color(0xFFB0BEC5)
        val moonColor = Color(0xFF9FA8DA)
        sr?.let { add(TideMarker(it, TideType.FLOW, sunColor)) }
        ss?.let { add(TideMarker(it, TideType.FLOW, sunColor)) }
        mr?.let { add(TideMarker(it, TideType.FLOW, moonColor)) }
        ms?.let { add(TideMarker(it, TideType.FLOW, moonColor)) }
    }
}

/** 낮/달 떠있는 시간대를 링에 ‘세그먼트’로 칠하기 */
fun TideInfoData.toSunMoonSegments(): List<TideSegment> {
    val (sr, ss) = sunTimes()
    val (mr, ms) = moonTimes()
    val segs = mutableListOf<TideSegment>()
    if (sr != null && ss != null) {
        segs += TideSegment(sr, ss, Color(0xFFB0BEC5).copy(alpha = 0.28f))
    }
    if (mr != null && ms != null) {
        segs += TideSegment(mr, ms, Color(0xFF9FA8DA).copy(alpha = 0.22f))
    }
    return segs
}

/* ---------- TideInfoData 파생 ---------- */

// jowi* 또는 pTime* 중 채워진 쪽을 사용
// 공통 추출
private fun TideInfoData.tideStrings(): List<String> = listOf(
    pTime1.ifBlank { jowi1 },
    pTime2.ifBlank { jowi2 },
    pTime3.ifBlank { jowi3 },
    pTime4.ifBlank { jowi4 },
).filter { it.isNotBlank() }
// 견고한 파서 (1~2자리 시, 어디에 있어도 hh:mm 찾음)
private val TIME_RE = Regex("(\\d{1,2}:\\d{2})")

fun TideInfoData.toCalloutItems(): List<CalloutItem> =
    tideStrings().mapNotNull { raw ->
        val timeStr = TIME_RE.find(raw)?.groupValues?.get(1) ?: return@mapNotNull null
        val t = runCatching { LocalTime.parse(timeStr, java.time.format.DateTimeFormatter.ofPattern("H:mm")) }.getOrNull()
            ?: return@mapNotNull null
        val isHigh = '▲' in raw
        val isLow  = '▼' in raw
        val symbol = when { isHigh -> "▲"; isLow -> "▼"; else -> "•" }
        val color  = when { isHigh -> Color(0xFF1E88E5); isLow -> Color(0xFFE53935); else -> Color(0xFFB0BEC5) }
        CalloutItem(time = t, text = "$symbol $timeStr", color = color)
    }

fun TideInfoData.toMarkers(): List<TideMarker> =
    tideStrings().mapNotNull { raw ->
        val timeStr = TIME_RE.find(raw)?.groupValues?.get(1) ?: return@mapNotNull null
        val t = runCatching { LocalTime.parse(timeStr, java.time.format.DateTimeFormatter.ofPattern("H:mm")) }.getOrNull()
            ?: return@mapNotNull null
        val isHigh = '▲' in raw
        val isLow  = '▼' in raw
        TideMarker(
            time = t,
            type = when { isHigh -> TideType.HIGH; isLow -> TideType.LOW; else -> TideType.FLOW },
            color = when { isHigh -> Color(0xFF1E88E5); isLow -> Color(0xFFE53935); else -> Color.Gray }
        )
    }

/* ---------- 화면 루트 ---------- */
@Composable
fun TideWatchScreen(
    navController: NavController,
    tideVM: TideViewModel
) {
    val tideState = tideVM.uiState.value
    val context = LocalContext.current
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var centerTime by remember {
        mutableStateOf(
            ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .toLocalTime()
                .withSecond(0)
                .withNano(0)
        )
    }
    var showLegend by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }
    val today = tideState.tideList.getOrNull(currentIndex)

    LaunchedEffect(today) {
        android.util.Log.d("TideWatch", "callouts=${today?.toCalloutItems()?.map { it.text }}")
    }
    LaunchedEffect(Unit) {
        (context as MainActivity).requestTide()
        while (true) {
            centerTime = LocalTime.now()
            delay(1000L)
        }
    }

    // 변환된 데이터
    val baseMarkers = today?.toMarkers() ?: emptyList()
    val sunMoonMarkers = today?.toSunMoonMarkers() ?: emptyList()
    val markers = baseMarkers + sunMoonMarkers

    val segments = today?.toSunMoonSegments() ?: emptyList()
    val density = LocalDensity.current
    val navigateThresholdPx = with(density) { 48.dp.toPx() } // 약 48dp
    var dragAccum by remember { mutableStateOf(0f) }

    val dragModifier = Modifier.pointerInput(today) {
        detectVerticalDragGestures(
            onDragStart = { dragAccum = 0f },
            onVerticalDrag = { _, dy -> dragAccum += dy }, // 아래로 +, 위로 -
            onDragEnd = {
                // 위로 스와이프(음수 누적)일 때 상세 "times" 페이지로 이동
                if (dragAccum <= -navigateThresholdPx && today != null) {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selectedTide", today)
                    navController.navigate("tide/times") { launchSingleTop = true }
                }
                dragAccum = 0f
            },
            onDragCancel = { dragAccum = 0f }
        )
    }
    SwipeToDismissBox(onDismissed = { navController.popBackStack() }) {
        Scaffold(
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
        ) {
            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .then(dragModifier) // ← 이 dragModifier는 위에서 방금 만든 '누적' 버전
            ) {
                val density = LocalDensity.current
                val locationLabel = today?.pName?.takeIf { it.isNotBlank() } ?: "위치정보없음"
                val minSidePx = with(density) { if (maxWidth < maxHeight) maxWidth.toPx() else maxHeight.toPx() }
                val dialDp = with(density) { (minSidePx * 0.54f).toDp() }
                val ringRadiusDp = dialDp / 2
                // ▶ 라벨을 더 바깥으로
                val outerLabelRadius = ringRadiusDp + 14.dp
                // --- 레인(겹침 방지) 계산 ---
                Box(Modifier.fillMaxSize()) {

//                    Icon(
//                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
//                        contentDescription = "next day",
//                        tint = Color.White,
//                        modifier = Modifier
//                            .align(Alignment.CenterEnd)
//                            .padding(end = 8.dp)
//                            .size(18.dp)
//                            .clickable {
//                                if (currentIndex < tideState.tideList.lastIndex) {
//                                    currentIndex++
//                                    tideState.tideList.getOrNull(currentIndex)?.let { next ->
//                                        currentDate = parsePThisDate(next.pThisDate) ?: currentDate
//                                    }
//                                }
//                            }
//                    )
//
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
//                        contentDescription = "prev day",
//                        tint = Color.White,
//                        modifier = Modifier
//                            .align(Alignment.CenterStart)
//                            .padding(start = 8.dp)
//                            .size(18.dp)
//                            .clickable {
//                                if (currentIndex > 0) {
//                                    currentIndex--
//                                    tideState.tideList.getOrNull(currentIndex)?.let { prev ->
//                                        currentDate = parsePThisDate(prev.pThisDate) ?: currentDate
//                                    }
//                                }
//                            }
//                    )


//                    Icon(
//                        imageVector = Icons.Default.ArrowDownward,
//                        contentDescription = "detail page",
//                        tint = Color.White,
//                        modifier = Modifier
//                            .align(Alignment.BottomCenter)
//                            .padding(bottom = 16.dp)
//                            .size(22.dp)
//                            .clickable {
//                                today?.let {
//                                    navController.currentBackStackEntry
//                                        ?.savedStateHandle
//                                        ?.set("selectedTide", it)
//                                    navController.navigate("tideDetail")
//                                }
//                            }
//                    )

                    TideDial(
                        diameter = dialDp,
                        centerTime = centerTime,
                        date = currentDate,
                        segments = segments,
                        markers = markers,
                        modifier = Modifier.align(Alignment.Center),
                        onPrevDay = {
                            if (currentIndex > 0) {
                                currentIndex--
                                tideState.tideList.getOrNull(currentIndex)?.let { prev ->
                                    currentDate = parsePThisDate(prev.pThisDate) ?: currentDate
                                }
                            }
                        },
                        onNextDay = {
                            if (currentIndex < tideState.tideList.lastIndex) {
                                currentIndex++
                                tideState.tideList.getOrNull(currentIndex)?.let { next ->
                                    currentDate = parsePThisDate(next.pThisDate) ?: currentDate
                                }
                            }
                        },
                        onInfoClick = { showLegend = true },
                        locationLabel = locationLabel
                    )


                    val anchorHours = remember { listOf(0, 3, 6, 9, 12, 15, 18, 21) }
                    val hourItems = remember(anchorHours) {
                        anchorHours.map { h ->
                            CalloutItem(
                                time  = LocalTime.of(h % 24, 0),
                                text  = h.toString(),            // 순수 숫자만
                                color = Color(0xFFB0BEC5)
                            )
                        }
                    }
                    SideCallouts(
                        items = hourItems,
                        radius = ringRadiusDp +7.dp,
                        modifier = Modifier.align(Alignment.Center),
                        labelPadDp = 0.dp,
                        nudgeDp = 0.dp,
                        numbersOnly = true,                      // 점(•) 없애기
                        fontSizeSp = 6                        // 더 작게
                    )

                    // 바깥쪽 라벨: 조석 + 일/월 출몰
                    val calloutItems = remember(today) {
                        (today?.toCalloutItems().orEmpty()) + (today?.toSunMoonCallouts().orEmpty())
                    }
                    SideCallouts(
                        items = calloutItems,
                        radius = outerLabelRadius,
                        modifier = Modifier.align(Alignment.Center),
                        labelPadDp = 9.dp,
                        nudgeDp = 2.dp,
                        showSunMoonIcons = true,    // ⬅ 아이콘 사용 ON
                        iconSizeDp = 10.dp          // ⬅ 필요시 8~12dp 사이로 미세조정
                    )




                    // 오버레이 (설명창)
                    if (showLegend) {
                        LegendOverlay(onDismiss = { showLegend = false })
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
    modifier: Modifier = Modifier,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onInfoClick: () -> Unit,
    locationLabel: String
) {
    val ringW = 12f
    val tickW = 2f   // ✅ 기본 눈금 굵기 복구

    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rOuter = size.minDimension / 2f
            val rRing = rOuter - ringW

            // ✅ 24시간 눈금 (기존 스타일: 3시간마다만 살짝 길고 굵게)
            repeat(24) { h ->
                val a = hourToAngleRad(h.toFloat())
                val is3h = (h % 3 == 0)

                val r1 = rRing - if (is3h) 16f else 14f
                val r2 = rRing + if (is3h) 16f else 14f

                val x1 = cx + r1 * cos(a)
                val y1 = cy + r1 * sin(a)
                val x2 = cx + r2 * cos(a)
                val y2 = cy + r2 * sin(a)

                drawLine(
                    color = if (is3h) Color(0xFF7A7A7A) else Color(0xFF3A3A3A),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = if (is3h) 3f else tickW,
                    cap = StrokeCap.Butt
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

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "prev day",
                    tint = Color.White,
                    modifier = Modifier
                        .size(14.dp)           // 아이콘 작게
                        .clickable { onPrevDay() }
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "%d.%d.%d".format(date.year, date.monthValue, date.dayOfMonth),
                    color = Color.White,
                    // 기존 caption1 → 더 작게
                    style = MaterialTheme.typography.caption2.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "next day",
                    tint = Color.White,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onNextDay() }
                )
            }

            Spacer(Modifier.height(2.dp))
            Text(
                text = "%d:%02d".format(centerTime.hour, centerTime.minute),
                color = Color.White,
                style = MaterialTheme.typography.title2,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = locationLabel,
                color = Color(0xFFE53935),
                style = MaterialTheme.typography.caption2
                    .copy(fontSize = 6.sp, fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                softWrap = false
            )



        }
        InfoCircle(
            onClick = onInfoClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 10.dp)   // 다이얼 밖으로 살짝 내림(필요하면 8~12dp 조절)
        )
    }
}

/* ---------- 바깥 라벨 (시간 작은 표기) ---------- */
@OptIn(ExperimentalTextApi::class)
@Composable
private fun SideCallouts(
    items: List<CalloutItem>,
    radius: Dp,
    modifier: Modifier = Modifier,
    labelPadDp: Dp = 10.dp,
    nudgeDp: Dp = 2.dp,
    numbersOnly: Boolean = false,
    fontSizeSp: Int? = null,
    showSunMoonIcons: Boolean = false,
    iconSizeDp: Dp = 10.dp,
    avoidOverlap: Boolean = true,     // 겹침 방지 ON/OFF
    laneGapDp: Dp = 8.dp,             // 레인 간 반지름 간격
    minSepDeg: Float = 12f,          // 같은 레인에서 허용할 최소 각도 간격
    tangentNudgeDp: Dp = 6.dp
)

{
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val baseStyle = MaterialTheme.typography.caption2.copy(
        fontSize = 10.sp,
        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
    )
    // --- 레인(겹침 방지) 계산 ---
    val laneOf = remember(items, avoidOverlap, minSepDeg) {
        val result = IntArray(items.size) { 0 }
        if (!avoidOverlap || items.isEmpty()) return@remember result

        data class ToPlace(val idx: Int, val angleDeg: Float)
        data class Lane(var lastDeg: Float = -9999f)

        val toPlace = items.mapIndexed { i, it ->
            val deg = Math.toDegrees(timeToAngleRad(it.time).toDouble()).toFloat()
            ToPlace(i, deg)
        }.sortedBy { it.angleDeg }

        val lanes = mutableListOf<Lane>()
        toPlace.forEach { p ->
            var placed = false
            for (laneIdx in lanes.indices) {
                val gap = kotlin.math.abs(p.angleDeg - lanes[laneIdx].lastDeg)
                if (gap >= minSepDeg) {
                    result[p.idx] = laneIdx
                    lanes[laneIdx].lastDeg = p.angleDeg
                    placed = true
                    break
                }
            }
            if (!placed) {
                val newIdx = lanes.size
                result[p.idx] = newIdx
                lanes += Lane(p.angleDeg)
            }
        }
        result
    }

    val timeStyle  = if (fontSizeSp != null) baseStyle.copy(fontSize = fontSizeSp.sp) else baseStyle
    val symbolStyle = timeStyle  // 동일 스타일 사용

    data class LayoutPair(
        val symW: Float, val symH: Float,
        val timeW: Float, val timeH: Float
    )

    // ---- 추가: 도우미 ----
    fun isSunMoonLabel(s: String): Boolean =
        s.startsWith("일출") || s.startsWith("일몰") || s.startsWith("월출") || s.startsWith("월몰")

    fun split(item: CalloutItem): Pair<String, String> {
        if (numbersOnly) return "" to item.text
        // 출몰 라벨이면 아이콘으로 처리 → 기호는 "" 로 비우고, 시간만 반환
        if (showSunMoonIcons && (
                    item.text.startsWith("일출") || item.text.startsWith("일몰") ||
                            item.text.startsWith("월출") || item.text.startsWith("월몰")
                    )
        ) {
            val timeStr = TIME_RE.find(item.text)?.groupValues?.get(1) ?: item.text
            return "" to timeStr
        }
        // 기존 기호(▲▼•)
        val first = item.text.firstOrNull()
        val hasSymbol = first == '▲' || first == '▼' || first == '•'
        val symbol = if (hasSymbol) first.toString() else ""
        val timeStr = if (hasSymbol) item.text.drop(1).trim() else item.text
        return symbol to timeStr
    }


// ---- 측정 로직: 출몰아이콘이면 아이콘 크기로 심볼 W/H 설정 ----
    val measured = remember(items, timeStyle, symbolStyle, numbersOnly, showSunMoonIcons, iconSizeDp) {
        val iconPx = with(density) { iconSizeDp.toPx() }
        items.map { item ->
            val (sym, timeStr) = split(item)
            val isIcon = showSunMoonIcons && (
                    item.text.startsWith("일출") || item.text.startsWith("일몰") ||
                            item.text.startsWith("월출") || item.text.startsWith("월몰")
                    )
            val symW = if (isIcon) iconPx else if (sym.isEmpty()) 0f else
                textMeasurer.measure(AnnotatedString(sym), style = symbolStyle).size.width.toFloat()
            val symH = if (isIcon) iconPx else if (sym.isEmpty()) 0f else
                textMeasurer.measure(AnnotatedString(sym), style = symbolStyle).size.height.toFloat()
            val timeLayout = textMeasurer.measure(AnnotatedString(timeStr), style = timeStyle)
            LayoutPair(
                symW = symW,
                symH = symH,
                timeW = timeLayout.size.width.toFloat(),
                timeH = timeLayout.size.height.toFloat()
            )
        }
    }




    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val centerX = with(density) { (maxWidth / 2).toPx() }
        val centerY = with(density) { (maxHeight / 2).toPx() }

        val rPx = with(density) { (radius + labelPadDp).toPx() }
        val nudgePx = with(density) { nudgeDp.toPx() }

        items.forEachIndexed { i, item ->
            // 1) 라벨 텍스트 분해 (기호/시간)
            val (sym, timeStr) = split(item)

            // 2) 각도/좌표 계산
            val a = timeToAngleRad(item.time)
            val cosA = kotlin.math.cos(a)
            val sinA = kotlin.math.sin(a)

// 레인(겹침 방지) 반영
            val laneIdx = if (avoidOverlap) laneOf[i] else 0
            val laneOffsetPx = with(density) { (laneGapDp * laneIdx).toPx() }
            val rLane = rPx + laneOffsetPx

// 기본(반지름) 위치
            val baseCx = centerX + rLane * cosA
            val baseCy = centerY + rLane * sinA + if (sinA > 0) nudgePx else -nudgePx

// 🔸추가: 접선(tangent) 방향으로 laneIdx만큼 살짝 이동
//  - 각도에 대한 단위 접선 벡터 = (-sin, cos)
//  - 레인 0은 0, 레인 1부터는 좌우로 번갈아가며 밀어 겹침 완화
            val sign = if (laneIdx % 2 == 0) -1f else 1f
            val tNudge = with(density) { (tangentNudgeDp * laneIdx).toPx() } * sign
            val tx = (-sinA * tNudge).toFloat()
            val ty = ( cosA * tNudge).toFloat()

// 최종 중심점
            val cx = baseCx + tx
            val cy = baseCy + ty

            // 3) 측정값 꺼내기
            val m = measured[i]

            // 4) 아이콘 여부 판단 + 간격
            val isIcon = showSunMoonIcons && (
                    item.text.startsWith("일출") || item.text.startsWith("일몰") ||
                            item.text.startsWith("월출") || item.text.startsWith("월몰")
                    )
            val gapPx = if (!isIcon && sym.isEmpty()) 0f else with(density) { 2.dp.toPx() }

            // 5) 배치 계산
            val totalW = m.symW + gapPx + m.timeW
            val maxH = maxOf(m.symH, m.timeH)
            val anchorX = cx - totalW / 2f
            val baselineY = cy + (maxH / 2f)

            // 6) 심볼(아이콘/기호) 그리기
            if (isIcon) {
                val symX = anchorX
                val symY = baselineY - m.symH
                val img = if (item.text.startsWith("월")) Icons.Filled.DarkMode else Icons.Filled.WbSunny

                Icon(
                    imageVector = img,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier
                        .absoluteOffset(
                            x = with(density) { symX.toDp() },
                            y = with(density) { symY.toDp() }
                        )
                        .size(iconSizeDp)
                )
            } else if (sym.isNotEmpty()) {
                val symX = anchorX
                val symY = baselineY - m.symH
                Text(
                    text = sym,
                    color = item.color,
                    style = symbolStyle,
                    modifier = Modifier.absoluteOffset(
                        x = with(density) { symX.toDp() },
                        y = with(density) { symY.toDp() }
                    )
                )
            }

            // 7) 시간 텍스트
            val timeX = anchorX + m.symW + gapPx
            val timeY = baselineY - m.timeH
            Text(
                text = timeStr,
                color = item.color,
                style = timeStyle,
                modifier = Modifier.absoluteOffset(
                    x = with(density) { timeX.toDp() },
                    y = with(density) { timeY.toDp() }
                )
            )
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
            .background(Color(0xFF2A2A2A), shape = RoundedCornerShape(50))
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

@Composable
fun LegendOverlay(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)) // 반투명 배경
            .clickable(onClick = onDismiss),            // 바깥 클릭 시 닫기
        contentAlignment = Alignment.Center
    ) {
        // 안쪽 네모
        Box(
            modifier = Modifier
                .width(160.dp)
                .wrapContentHeight()
                .background(Color(0xFF262626), RoundedCornerShape(24.dp))
                .padding(12.dp)
                .clickable(enabled = false) {} // 안쪽은 클릭 이벤트 무시
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("표시 설명", color = Color.White, fontSize = 12.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("▲", color = Color(0xFF1E88E5), fontSize = 12.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("만조", color = Color.White, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("▼", color = Color(0xFFE53935), fontSize = 12.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("간조", color = Color.White, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "일출·일몰",
                        tint = Color(0xFFB0BEC5),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("일출·일몰", color = Color.White, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DarkMode,
                        contentDescription = "월출·월몰",
                        tint = Color(0xFF9FA8DA),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("월출·월몰", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}


    @Composable
private fun LegendRow(color: Color, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/* ---------- 각도/시간 변환 ---------- */

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
