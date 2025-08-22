package com.example.dive_app.ui.screen.location

import androidx.compose.material3.Text
import kotlinx.coroutines.delay
import android.location.Location
import android.graphics.PointF
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
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.util.LocationUtil
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage

private enum class ViewMode { CURRENT, FISHING }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CurrentLocationScreen(
    locationViewModel: LocationViewModel,
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

    var showInfoBox by remember { mutableStateOf(true) }
//    LaunchedEffect(Unit) { delay(3000); showInfoBox = false }

    // 주소 라벨
    LaunchedEffect(latitude, longitude) {
        LocationUtil.fetchAddressFromCoords(latitude, longitude) { r1, r2 ->
            region1 = r1; region2 = r2
        }
    }

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

    // 모드 바꾸면 안내 패널 3초 다시
    LaunchedEffect(mode) { showInfoBox = true; delay(3000); showInfoBox = false }

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
                    // 현재 모드에 맞춰 핀치줌 초기 설정
                    nMap.uiSettings.isZoomGesturesEnabled = (mode != ViewMode.CURRENT)

                    // 현위치 오버레이
                    nMap.locationOverlay.apply {
                        isVisible = true
                        position = LatLng(latitude, longitude)
                    }

                    // 최초 1회만 내 위치로 이동
                    if (!cameraInitialized) {
                        nMap.moveCamera(
                            CameraUpdate.scrollTo(LatLng(latitude, longitude))
                                .animate(CameraAnimation.Easing)
                        )
                        cameraInitialized = true
                    }

                    // 마커 재생성
                    fishingMarkers.forEach { it.map = null }
                    fishingMarkers.clear()

                    if (mode == ViewMode.FISHING && hasPoints) {
                        if (inSingle) {
                            // 하나씩 보기: 현재 포인트 하나만 표시
                            currentFP?.let { fp ->
                                fishingMarkers += Marker().apply {
                                    position = LatLng(fp.lat, fp.lon)
                                    icon = OverlayImage.fromResource(
                                        com.naver.maps.map.R.drawable.navermap_default_marker_icon_green
                                    )
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
                        if (nearby.isNotEmpty()) {
                            idx = 0               // 첫 포인트 선택
                            showInfoBox = false
                            naverMapRef?.moveCamera(
                                CameraUpdate.scrollTo(LatLng(nearby[0].lat, nearby[0].lon))
                                    .animate(CameraAnimation.Easing)
                            )
                        } else {
                            idx = -1
                            showInfoBox = true
                        }
                    } else {
                        // CURRENT로 전환 시
                        idx = -1
                        showInfoBox = true
                        naverMapRef?.moveCamera(
                            CameraUpdate.scrollTo(LatLng(latitude, longitude))
                                .animate(CameraAnimation.Easing)
                        )
                    }
                }
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
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

        // 좌/우/가운데 탭 영역 (상/하 100dp 가드)
        if (mode == ViewMode.FISHING && hasPoints) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f)
                    .pointerInput(hasPoints, inSingle) {
                        detectTapGestures { offset ->
                            val w = size.width
                            val h = size.height
                            val topGuard = 100.dp.toPx()
                            val bottomGuard = 100.dp.toPx()
                            if (offset.y < topGuard || offset.y > h - bottomGuard) return@detectTapGestures

                            when {
                                // 오른쪽 탭 → 하나씩 보기 진입/다음
                                offset.x > w * 0.75f -> {
                                    idx = if (!inSingle) 0 else (idx + 1) % nearby.size
                                    showInfoBox = false
                                }
                                // 왼쪽 탭 → 하나씩 보기 상태에서만 이전
                                offset.x < w * 0.25f -> {
                                    if (inSingle) {
                                        idx = (idx - 1 + nearby.size) % nearby.size
                                        showInfoBox = false
                                    }
                                }
                                // 가운데 탭 → 하나씩 보기 종료(전체 마커 복귀)
                                else -> {
                                    if (inSingle) {
                                        idx = -1
                                        naverMapRef?.moveCamera(
                                            CameraUpdate.scrollTo(LatLng(latitude, longitude))
                                                .animate(CameraAnimation.Easing)
                                        )
                                    }
                                }
                            }
                        }
                    }
            )
        }

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
                        topStart = 30.dp, topEnd = 30.dp,
                        bottomStart = h, bottomEnd = h
                    )

                    Box(
                        modifier = Modifier
                            .width(w)
                            .height(h)
                            .shadow(14.dp, shape)
                            .clip(shape)
                            .background(Color(0xF01A1A1A))
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentFP?.point_nm ?: "-",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = currentFP?.point_dt ?: "",
                                color = Color(0xFF58CCFF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${idx + 1} / ${nearby.size}",
                                color = Color(0xFFBDBDBD),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 초기/모드전환 안내 패널 (포인트 선택되면 숨김)
        AnimatedVisibility(
            visible = showInfoBox && !inSingle,
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
                            text = if (mode == ViewMode.FISHING)
                                "오른쪽을 탭하면 한 개씩 보기"
                            else region1,
                            fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                        )
                        if (mode == ViewMode.CURRENT && region2.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(text = region2, fontSize = 13.sp, color = Color(0xFFE0E0E0))
                        }
                    }
                }
            }
        }

        // 🔹 CURRENT 모드에서만: 지도 영역에서만 더블탭 리센터 (상/하 가드)
        if (mode == ViewMode.CURRENT) {
            val topGuard = 100.dp      // 상단 UI 보호 영역 (칩 등)
            val bottomGuard = 120.dp   // 하단 그라데이션/패널 보호 영역

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topGuard, bottom = bottomGuard) // ← 버튼/패널 안 덮게!
                    .zIndex(5f)
                    .pointerInput(mode, latitude, longitude) {
                        detectTapGestures(
                            onDoubleTap = {
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

private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val out = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, out)
    return out[0]
}
