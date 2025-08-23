package com.example.dive_app.ui.screen.location
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

import androidx.compose.material3.Text
import kotlinx.coroutines.delay
import android.location.Location
import android.graphics.PointF
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.dive_app.MainActivity
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.domain.viewmodel.TideViewModel
import com.example.dive_app.domain.viewmodel.WeatherViewModel
import com.example.dive_app.util.FishingAnalyzer
import com.example.dive_app.util.LocationUtil
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import java.time.LocalDateTime

private enum class ViewMode { CURRENT, FISHING }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CurrentLocationScreen(
    navController : NavController,
    locationViewModel: LocationViewModel,
    weatherViewModel : WeatherViewModel,
    tideViewModel : TideViewModel,
    points: List<FishingPoint>,               // ← 실제 API 데이터 주입
    onMarkerClick: (FishingPoint) -> Unit,
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    var region1 by remember { mutableStateOf("로딩중...") }
    var region2 by remember { mutableStateOf("") }
    val loc by locationViewModel.location.observeAsState()
    val latitude = loc?.first ?: 35.1151
    val longitude = loc?.second ?: 129.0415
    var mode by remember { mutableStateOf(ViewMode.CURRENT) }

    // 안내 패널: 처음/모드전환 후 3초 표시(포인트 선택되면 즉시 숨김)
    var showInfoBox by remember { mutableStateOf(true) }
//    LaunchedEffect(Unit) { delay(3000); showInfoBox = false }


    // 주소 라벨
    LaunchedEffect(latitude, longitude) {
        (context as MainActivity).requestLocation()
        (context as MainActivity).requestTide()
        (context as MainActivity).requestWeather()
        LocationUtil.fetchAddressFromCoords(latitude, longitude) { r1, r2 ->
            region1 = r1; region2 = r2
        }
    }
    val weather by weatherViewModel.uiState
    val tide by tideViewModel.uiState

    // NaverMap 참조 & 카메라 초기화
    var naverMapRef by remember { mutableStateOf<NaverMap?>(null) }
    var cameraInitialized by remember { mutableStateOf(false) }

    // 우리가 만든 마커들
    val fishingMarkers = remember { mutableStateListOf<Marker>() }

    // 가까운 순 리스트 (내 위치와 30m 미만 제외)
    val nearby by remember(latitude, longitude, points) {
        mutableStateOf(
            points
                .filter { p -> p.lat != 0.0 || p.lon != 0.0 }
                .filter { p -> distanceMeters(latitude, longitude, p.lat, p.lon) >= 30f }
                .map { fp ->
                    val d = distanceMeters(latitude, longitude, fp.lat, fp.lon)
                    fp.copy(point_dt = String.format("%.1f km", d / 1000f))
                }
                .sortedBy { distanceMeters(latitude, longitude, it.lat, it.lon) }
        )
    }

    // ‘하나씩 보기’ 인덱스: -1 이면 전체 마커 모드
    var idx by remember { mutableStateOf(-1) }
    val hasPoints = nearby.isNotEmpty()
    val inSingle = idx >= 0
    val currentFP: FishingPoint? = if (inSingle) nearby[idx] else null

    LaunchedEffect(mode) {
        if (mode == ViewMode.CURRENT) {
            showInfoBox = true
//            delay(3000)
//            showInfoBox = false
        } else {
            showInfoBox = false
        }
    }

    // 모드 전환 시 핀치 줌 on/off (CURRENT에서 핀치줌 OFF, FISHING에서 ON)
    LaunchedEffect(mode) {
        naverMapRef?.uiSettings?.isZoomGesturesEnabled = (mode != ViewMode.CURRENT)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .clip(CircleShape)
    ) {
        // 지도
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { mv ->
                mv.getMapAsync { nMap ->
                    naverMapRef = nMap
                    nMap.uiSettings.isZoomControlEnabled = false
                    // 모드에 맞춰 핀치줌 초기 설정
                    nMap.uiSettings.isZoomGesturesEnabled = (mode != ViewMode.CURRENT)

                    // 현위치 오버레이
                    nMap.locationOverlay.apply {
                        isVisible = true
                        position = LatLng(latitude, longitude)
                    }

                    fishingMarkers.forEach { it.map = null }
                    fishingMarkers.clear()

                    val now = LocalDateTime.now()
                    val hour = now.hour
                    val month = now.monthValue

                    nearby.forEach { fp ->
                        fishingMarkers += Marker().apply {
                            position = LatLng(fp.lat, fp.lon)
                            icon = OverlayImage.fromResource(
                                com.naver.maps.map.R.drawable.navermap_default_marker_icon_green
                            )
                            width = 48
                            height = 64
                            anchor = PointF(0.5f, 1f)
                            zIndex = 1
                            val best = FishingAnalyzer.recommendFish(
                                target = fp.target,
                                temp = weather.obsWt?.toString()?.toDoubleOrNull() ?: 19.0,   // 수온
                                current = 2.4,//tide.wavePrd?.toString()?.toDoubleOrNull() ?: 1.0,  // 조류
                                hour = hour,        // 현재 시간
                                mul = tide.tideList.firstOrNull()?.pMul
                                    ?.toString()
                                    ?.replace("[^0-9]".toRegex(), "")
                                    ?.takeIf { it.isNotBlank() }   // ✅ 빈 문자열이면 null 처리
                                    ?.toIntOrNull() ?: 1,
                                month = month
                            )
                            // ✅ 마커 캡션에 추천어종 표시
                            Log.d("FishingDebug", "📍 point=${fp.point_nm}, ${fp.target} lat=${fp.lat}, lon=${fp.lon}, best=$best")

                            captionText = best ?: ""

                            captionColor = android.graphics.Color.BLACK
                            captionTextSize = 12f

                            setOnClickListener(Overlay.OnClickListener {
                                onMarkerClick(fp); true
                            })
                            map = nMap
                        }
                    }


                    // 최초 1회만 내 위치로 이동
                    if (!cameraInitialized) {
                        nMap.moveCamera(
                            CameraUpdate.scrollTo(LatLng(latitude, longitude))
                                .animate(CameraAnimation.Easing)
                        )
                        nMap.moveCamera(CameraUpdate.zoomTo(12.5))
                        cameraInitialized = true
                    }

                    // 마커 재생성
                    //fishingMarkers.forEach { it.map = null }
                    //fishingMarkers.clear()

                    if (mode == ViewMode.FISHING && hasPoints) {
                        if (inSingle) {
                            // 하나씩 보기: 현재 포인트 하나만 표시
                            currentFP?.let { fp ->
                                fishingMarkers += Marker().apply {
                                    position = LatLng(fp.lat, fp.lon)
                                    icon = OverlayImage.fromResource(
                                        com.naver.maps.map.R.drawable.navermap_default_marker_icon_green
                                    )
                                    width = 48
                                    height = 64
                                    anchor = PointF(0.5f, 1f)
                                    zIndex = 1
                                    setOnClickListener(Overlay.OnClickListener {
                                        onMarkerClick(fp); true
                                    })
                                    map = nMap
                                }
                            }
                        } else {
                            // 전체 마커 표시
                            nearby.forEach { fp ->
                                fishingMarkers += Marker().apply {
                                    position = LatLng(fp.lat, fp.lon)
                                    icon = OverlayImage.fromResource(
                                        com.naver.maps.map.R.drawable.navermap_default_marker_icon_green
                                    )
                                    width = 48
                                    height = 64
                                    anchor = PointF(0.5f, 1f)
                                    zIndex = 1
                                    setOnClickListener(Overlay.OnClickListener {
                                        onMarkerClick(fp); true
                                    })
                                    map = nMap
                                }
                            }
                        }
                    }
                }
            }
        )

        // 하단 가독성 그라데이션
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x33000000), Color(0x99000000))
                    )
                )
        )

        // 상단: 모드 토글 칩(한 개만)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .zIndex(2f)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF2B2B2B))
                .clickable {
                    val newMode = if (mode == ViewMode.CURRENT) ViewMode.FISHING else ViewMode.CURRENT
                    mode = newMode

                    if (newMode == ViewMode.FISHING) {
                        idx = if (nearby.isNotEmpty()) 0 else -1
                        // FISHING 전환 시 안내 패널 안 뜨게 확실히 끔
                        showInfoBox = false

                        if (nearby.isNotEmpty()) {
                            naverMapRef?.moveCamera(
                                CameraUpdate.scrollTo(LatLng(nearby[0].lat, nearby[0].lon))
                                    .animate(CameraAnimation.Easing)
                            )
                        }
                    } else {
                        idx = -1
                        naverMapRef?.moveCamera(
                            CameraUpdate.scrollTo(LatLng(latitude, longitude))
                                .animate(CameraAnimation.Easing)
                        )
                        // CURRENT로 돌아오면 위의 LaunchedEffect(mode)가 3초 표시를 맡음
                    }
                }
                .padding(horizontal = 18.dp, vertical = 10.dp)
        )  {
            if (mode == ViewMode.FISHING) {
                Text(
                    text = "낚시포인트",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "현위치",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

//        // ▶ 좌/우 화살표 네비게이션 (낚시포인트 모드에서만 표시)
//        if (mode == ViewMode.FISHING && hasPoints) {
//            // 왼쪽 화살표
//            Box(
//                modifier = Modifier
//                    .align(Alignment.CenterStart)
//                    .padding(start = 8.dp)
//                    .size(36.dp)
//                    .clip(CircleShape)
////                    .background(Color(0x66000000))
//                    .then(
//                        if (inSingle) Modifier.clickable {
//                            idx = (idx - 1 + nearby.size) % nearby.size
//                            showInfoBox = false
//                        } else Modifier
//                    )
//                    .zIndex(3f),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
//                    contentDescription = "이전",
//                    tint = if (inSingle) Color.White else Color.White.copy(alpha = 0.4f)
//                )
//            }
//
//            // 오른쪽 화살표
//            Box(
//                modifier = Modifier
//                    .align(Alignment.CenterEnd)
//                    .padding(end = 8.dp)
//                    .size(36.dp)
//                    .clip(CircleShape)
////                    .background(Color(0x66000000))
//                    .clickable {
//                        idx = if (!inSingle) 0 else (idx + 1) % nearby.size
//                        showInfoBox = false
//                    }
//                    .zIndex(3f),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
//                    contentDescription = "다음",
//                    tint = Color.White
//                )
//            }
//        }

        // 인덱스 바뀌면 선택 포인트로 카메라 이동 (하나씩 보기일 때만)
        LaunchedEffect(idx, mode) {
            if (mode != ViewMode.FISHING) return@LaunchedEffect
            if (idx >= 0) {
                val fp = nearby[idx]
                naverMapRef?.moveCamera(
                    CameraUpdate.scrollTo(LatLng(fp.lat, fp.lon))
                        .animate(CameraAnimation.Easing)
                )
            }
        }

        // 하단 카드(이름/거리/인디케이터) — 하나씩 보기일 때만
        if (mode == ViewMode.FISHING && hasPoints && inSingle) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(), exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .padding(bottom = 13.dp)
                ) {
                    val w = maxWidth
                    val h = w / 2
                    val shape = RoundedCornerShape(
                        topStart = 40.dp, topEnd = 40.dp,
                        bottomStart = h, bottomEnd = h
                    )

                    Box(
                        modifier = Modifier
                            .width(w)
                            .height(h)
                            .shadow(14.dp, shape)
                            .clip(shape)
                            .background(Color(0xF01A1A1A))
                            .clickable {
                                currentFP?.let { fp ->
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("fishingPoint", fp)
                                    navController.navigate("fishingDetail")
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 위쪽: 포인트명
                            AutoResizeText(
                                text = currentFP?.point_nm ?: "-",
                                maxFontSize = 13.sp,
                                minFontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 중앙: 거리 + 좌우 화살표
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // 왼쪽 화살표
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "이전",
                                    tint = Color.White.copy(alpha = if (inSingle) 0.3f else 0f),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable(enabled = inSingle) {
                                            idx = (idx - 1 + nearby.size) % nearby.size
                                            showInfoBox = false
                                        }
                                )

                                // 거리
                                Text(
                                    text = currentFP?.point_dt ?: "",
                                    color = Color(0xFF58CCFF),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Clip,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                                )

                                // 오른쪽 화살표
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "다음",
                                    tint = Color.White.copy(alpha = if (inSingle) 0.3f else 0f),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            idx = if (!inSingle) 0 else (idx + 1) % nearby.size
                                            showInfoBox = false
                                        }
                                )
                            }

                            // 아래쪽: 인덱스
                            Text(
                                text = "${idx + 1} / ${nearby.size}",
                                color = Color(0xFFBDBDBD),
                                fontSize = 8.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // 초기/모드전환 안내 패널 (포인트 선택되면 숨김)
        AnimatedVisibility(
            visible = showInfoBox && mode == ViewMode.CURRENT && !inSingle,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .padding(bottom = 13.dp)
            ) {
                val w = maxWidth
                val h = w / 2
                val shape = RoundedCornerShape(
                    topStart = 30.dp, topEnd = 30.dp,
                    bottomStart = h, bottomEnd = h
                )
                Box(
                    modifier = Modifier
                        .width(w).height(h)
                        .shadow(14.dp, shape)
                        .clip(shape)
                        .background(Color(0xF01A1A1A))
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = region1,
                            fontSize =16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                        )
                        if (region2.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(text = region2, fontSize = 13.sp, color = Color(0xFFE0E0E0))
                        }
                    }
                }
            }
        }

        // CURRENT에서만: 지도 영역 더블탭 → 현재 위치로 리센터 (칩/하단 영역 보호)
        if (mode == ViewMode.CURRENT) {
            val topGuard = 100.dp      // 상단 UI 보호(칩)
            val bottomGuard = 120.dp   // 하단 그라데이션/패널 보호
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topGuard, bottom = bottomGuard)
                    .zIndex(5f)
                    .pointerInput(mode, latitude, longitude) { // mode를 key에 포함하여 재생성 보장
                        detectTapGestures(
                            onDoubleTap = {
                                // 이중 가드: 콜백 시점에 모드가 바뀌었으면 무시
                                if (mode != ViewMode.CURRENT) return@detectTapGestures
                                naverMapRef?.moveCamera(
                                    CameraUpdate.scrollTo(LatLng(latitude, longitude))
                                        .animate(CameraAnimation.Easing)
                                )
                            }
                        )
                    }
            )
        }
    }

    // 수명주기 정리
    DisposableEffect(mapView) {
        mapView.onCreate(null); mapView.onStart(); mapView.onResume()
        onDispose {
            fishingMarkers.forEach { it.map = null }
            fishingMarkers.clear()
            mapView.onPause(); mapView.onStop(); mapView.onDestroy()
        }
    }
    LaunchedEffect(latitude, longitude, mode) {
        if (mode == ViewMode.CURRENT) {
            naverMapRef?.moveCamera(
                CameraUpdate.scrollTo(LatLng(latitude, longitude))
            )
        }
    }
}

@Composable
private fun AutoResizeText(
    text: String,
    maxFontSize: TextUnit =13.sp,
    minFontSize: TextUnit = 3.sp,
    color: Color = Color.White,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    modifier: Modifier = Modifier
) {
    var fontSize by remember(text) { mutableStateOf<TextUnit>(maxFontSize) }
    var ready by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        fontWeight = fontWeight,
        fontSize = fontSize,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,       // ✅ … 대신 잘라내고 폰트 줄이기 트리거
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
        onTextLayout = { result ->
            if (!ready && result.didOverflowWidth) {
                val next = (fontSize.value - 1).sp
                if (next >= minFontSize) {
                    fontSize = next
                } else {
                    ready = true
                }
            } else {
                ready = true
            }
        }
    )
}



private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val out = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, out)
    return out[0]
}
