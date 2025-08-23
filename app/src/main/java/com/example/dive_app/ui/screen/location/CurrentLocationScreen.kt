package com.example.dive_app.ui.screen.location
import com.example.dive_app.util.FishingAnalyzer
import java.time.LocalDateTime

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Text
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
import com.example.dive_app.util.LocationUtil
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private enum class ViewMode { CURRENT, FISHING }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CurrentLocationScreen(
    navController : NavController,
    locationViewModel: LocationViewModel,
    weatherViewModel : WeatherViewModel,
    tideViewModel : TideViewModel,
    points: List<FishingPoint>,
    onMarkerClick: (FishingPoint) -> Unit,
    isAppFishingMode: Boolean = false
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    // 실제 위치 (fallback 제거: null이면 표시/이동하지 않음)
    val loc by locationViewModel.location.observeAsState()
    val lat: Double? = loc?.first
    val lon: Double? = loc?.second
    val weather by weatherViewModel.uiState
    val tide by tideViewModel.uiState

    // 주소 라벨
    var region1 by remember { mutableStateOf("위치 확인중...") }
    var region2 by remember { mutableStateOf("") }

    // 최초 1회: 데이터 요청 트리거 (위치/조석/날씨)
    LaunchedEffect(Unit) {
        (context as MainActivity).requestLocation()
        (context as MainActivity).requestTide()
        (context as MainActivity).requestWeather()
    }

    // 주소는 loc가 준비된 뒤에만 갱신
    LaunchedEffect(loc) {
        if (lat != null && lon != null) {
            LocationUtil.fetchAddressFromCoords(lat, lon) { r1, r2 ->
                region1 = r1; region2 = r2
            }
        } else {
            region1 = "위치 확인중..."
            region2 = ""
        }
    }

    var mode by remember { mutableStateOf(ViewMode.CURRENT) }
    var showInfoBox by remember { mutableStateOf(true) }

    // NaverMap 참조
    var naverMapRef by remember { mutableStateOf<NaverMap?>(null) }

    // 우리가 만든 마커들
    val fishingMarkers = remember { mutableStateListOf<Marker>() }

    // 가까운 순 리스트 (loc 없으면 빈 리스트)
    val nearby by remember(lat, lon, points) {
        mutableStateOf(
            if (lat != null && lon != null) {
                points
                    .filter { p -> p.lat != 0.0 || p.lon != 0.0 }
                    .filter { p -> distanceMeters(lat, lon, p.lat, p.lon) >= 30f }
                    .map { fp ->
                        val d = distanceMeters(lat, lon, fp.lat, fp.lon)
                        fp.copy(point_dt = String.format("%.1f km", d / 1000f))
                    }
                    .sortedBy { distanceMeters(lat, lon, it.lat, it.lon) }
            } else emptyList()
        )
    }

    // ‘하나씩 보기’ 인덱스
    var idx by remember { mutableStateOf(-1) }
    val hasPoints = nearby.isNotEmpty()
    val inSingle = idx >= 0
    val currentFP: FishingPoint? = if (inSingle) nearby[idx] else null

    // ★ 앱 모드에 따라 내부 ViewMode 동기화
    LaunchedEffect(isAppFishingMode) {
        mode = if (isAppFishingMode) ViewMode.FISHING else ViewMode.CURRENT
        // FISHING으로 진입할 땐 ‘현위치부터’ 시작
        idx = -1
        showInfoBox = (mode == ViewMode.CURRENT)

        // 카메라도 모드에 맞춰 정리
        val a = loc?.first
        val b = loc?.second
        if (mode == ViewMode.FISHING && a != null && b != null) {
            naverMapRef?.moveCamera(
                CameraUpdate.scrollTo(LatLng(a, b)).animate(CameraAnimation.Easing)
            )
        } else if (mode == ViewMode.CURRENT && a != null && b != null) {
            naverMapRef?.moveCamera(
                CameraUpdate
                    .toCameraPosition(
                        com.naver.maps.map.CameraPosition(LatLng(a, b), 12.5)
                    )
                    .animate(CameraAnimation.Easing)
            )
        }
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
                    nMap.uiSettings.isZoomGesturesEnabled = (mode != ViewMode.CURRENT)

                    // 현위치 오버레이: loc가 있을 때만 보이기/갱신
                    nMap.locationOverlay.apply {
                        isVisible = (lat != null && lon != null)
                        if (lat != null && lon != null) {
                            position = LatLng(lat, lon)
                        }
                    }

                    // 마커 재생성
                    fishingMarkers.forEach { it.map = null }
                    fishingMarkers.clear()

                    if (hasPoints) {
                        val addMarker: (FishingPoint) -> Unit = { fp ->
                            // 추천어종 계산
                            val now = LocalDateTime.now()
                            val hour = now.hour
                            val month = now.monthValue
                            val best = FishingAnalyzer.recommendFish(
                                target = fp.target,
                                temp   = weather.obsWt?.toString()?.toDoubleOrNull() ?: 19.0,
                                current = 2.4,
                                hour = hour,
                                mul = tide.tideList.firstOrNull()?.pMul
                                    ?.toString()
                                    ?.replace("[^0-9]".toRegex(), "")
                                    ?.takeIf { it.isNotBlank() }
                                    ?.toIntOrNull() ?: 1,
                                month = month
                            )

                            fishingMarkers += Marker().apply {
                                position = LatLng(fp.lat, fp.lon)
                                icon = OverlayImage.fromResource(
                                    com.naver.maps.map.R.drawable.navermap_default_marker_icon_green
                                )
                                width = 48
                                height = 64
                                anchor = PointF(0.5f, 1f)
                                zIndex = 1
                                captionText = best ?: ""
                                captionColor = android.graphics.Color.BLACK
                                captionTextSize = 12f
                                setOnClickListener(Overlay.OnClickListener {
                                    onMarkerClick(fp); true
                                })
                                map = nMap   // ✅ 여기서는 naverMapRef 말고 nMap을 직접 사용
                            }
                        }

                        if (mode == ViewMode.FISHING && inSingle) {
                            currentFP?.let(addMarker)
                        } else {
                            nearby.forEach(addMarker)   // ✅ CURRENT 모드에서도 전체 마커 표시
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

        // 상단: 모드 토글 칩
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
                        idx = -1                      // ★ 현위치부터 시작
                        showInfoBox = false
                        if (lat != null && lon != null) {
                            naverMapRef?.moveCamera(
                                CameraUpdate.scrollTo(LatLng(lat, lon))
                                    .animate(CameraAnimation.Easing)
                            )
                        }
                    } else {
                        idx = -1
                        // CURRENT로 돌아올 때도 loc가 있어야 이동
                        if (lat != null && lon != null) {
                            naverMapRef?.moveCamera(
                                CameraUpdate.scrollTo(LatLng(lat, lon))
                                    .animate(CameraAnimation.Easing)
                            )
                        }
                    }
                }
                .padding(horizontal = 18.dp, vertical = 10.dp)
        )  {
            Text(
                text = if (mode == ViewMode.FISHING) "낚시포인트" else "현위치",
                color = Color.White,
                fontSize = if (mode == ViewMode.FISHING) 12.sp else 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 인덱스 바뀌면 선택 포인트로 카메라 이동 (하나씩 보기)
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

        // 하단 카드 — 하나씩 보기일 때만
        if (mode == ViewMode.FISHING && hasPoints && inSingle) {
            AnimatedVisibility(
                visible = true, enter = fadeIn(), exit = fadeOut(),
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
                            .width(w).height(h)
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
                            AutoResizeText(
                                text = currentFP?.point_nm ?: "-",
                                maxFontSize = 13.sp,
                                minFontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
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

        // 초기/모드전환 안내 패널
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        if (region2.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(text = region2, fontSize = 13.sp, color = Color(0xFFE0E0E0))
                        }
                    }
                }
            }
        }

        // CURRENT에서만: 지도 더블탭 → 현위치 리센터 (loc가 있을 때만)
        if (mode == ViewMode.CURRENT) {
            val topGuard = 100.dp
            val bottomGuard = 120.dp
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topGuard, bottom = bottomGuard)
                    .zIndex(5f)
                    .pointerInput(mode, loc) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (mode != ViewMode.CURRENT) return@detectTapGestures
                                if (lat != null && lon != null) {
                                    naverMapRef?.moveCamera(
                                        CameraUpdate.scrollTo(LatLng(lat, lon))
                                            .animate(CameraAnimation.Easing)
                                    )
                                }
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

    // ⭐ 지도와 위치가 준비되면 CURRENT 모드에서 한 번 정확히 센터링
    LaunchedEffect(naverMapRef, loc, mode) {
        if (mode != ViewMode.CURRENT) return@LaunchedEffect
        val a = lat ?: return@LaunchedEffect
        val b = lon ?: return@LaunchedEffect
        naverMapRef?.moveCamera(
            CameraUpdate
                .toCameraPosition(com.naver.maps.map.CameraPosition(LatLng(a, b), 12.5))
                .animate(CameraAnimation.Easing)
        )
    }
    LaunchedEffect(mode, nearby, loc) {
        if (mode != ViewMode.FISHING) return@LaunchedEffect
        // nearby/loc가 바뀔 때마다(재계산 시) 루프가 재시작됩니다.
        var i = -1
        while (isActive && mode == ViewMode.FISHING) {
            val total = 1 + nearby.size   // 1(현위치) + 포인트 수
            i = (i + 1) % maxOf(total, 1)

            if (i == 0) {
                // 현위치 스텝
                idx = -1
                if (lat != null && lon != null) {
                    naverMapRef?.moveCamera(
                        CameraUpdate
                            .toCameraPosition(
                                com.naver.maps.map.CameraPosition(LatLng(lat, lon), 12.5)
                            )
                            .animate(CameraAnimation.Easing)
                    )
                }
            } else {
                // 포인트 스텝 (i-1 번째 포인트)
                val next = i - 1
                if (next in nearby.indices) {
                    idx = next
                    // 카메라 이동은 기존의 LaunchedEffect(idx, mode)에서 처리됨
                    // (수동 화살표 전환과 동일 동작 보장)
                }
            }

            delay(3000) // ★ 3초 간격
        }
    }

    // 위치 바뀌면 오버레이 위치 갱신
    LaunchedEffect(naverMapRef, lat, lon) {
        naverMapRef?.locationOverlay?.apply {
            isVisible = (lat != null && lon != null)
            if (lat != null && lon != null) position = LatLng(lat, lon)
        }
    }
}

@Composable
private fun AutoResizeText(
    text: String,
    maxFontSize: TextUnit = 13.sp,
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
        overflow = TextOverflow.Clip,
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
