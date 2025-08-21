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
    onMarkerClick: (FishingPoint) -> Unit
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
    LaunchedEffect(Unit) { delay(3000); showInfoBox = false }

    // 주소 라벨 로딩
    LaunchedEffect(latitude, longitude) {
        LocationUtil.fetchAddressFromCoords(latitude, longitude) { r1, r2 ->
            region1 = r1; region2 = r2
        }
    }

    // NaverMap 참조 & 카메라 초기화 플래그
    var naverMapRef by remember { mutableStateOf<NaverMap?>(null) }
    var cameraInitialized by remember { mutableStateOf(false) }

    // 우리가 만든 마커들 관리
    val fishingMarkers = remember { mutableStateListOf<Marker>() }

    // 예시 포인트(실데이터로 교체 가능)
    val rawPoints: List<FishingPoint> = remember(latitude, longitude) {
        listOf(
            FishingPoint(point_nm = "포인트 A", lat = latitude + 0.002,  lon = longitude + 0.001),
            FishingPoint(point_nm = "포인트 B", lat = latitude - 0.0015, lon = longitude - 0.002),
            FishingPoint(point_nm = "포인트 C", lat = latitude + 0.001,  lon = longitude - 0.0015)
        )
    }

    // 거리 계산 후 가까운 순으로 정렬(+문자열 km 세팅)
    val nearby by remember(latitude, longitude, rawPoints) {
        mutableStateOf(
            rawPoints
                .map { fp ->
                    val d = distanceMeters(latitude, longitude, fp.lat, fp.lon)
                    fp.copy(point_dt = String.format("%.1f km", d / 1000f))
                }
                .sortedBy { distanceMeters(latitude, longitude, it.lat, it.lon) }
        )
    }

    // 한 개씩 보기 인덱스: -1 = 아직 선택 안 함 (→ 카드/카메라 이동 없음)
    var idx by remember { mutableStateOf(-1) }
    val hasPoints = nearby.isNotEmpty()
    val currentFP: FishingPoint? = if (idx in nearby.indices) nearby[idx] else null

    // 모드 바꾸면 안내 패널 3초 다시
    LaunchedEffect(mode) { showInfoBox = true; delay(3000); showInfoBox = false }

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

                    // 현위치 오버레이
                    nMap.locationOverlay.apply {
                        isVisible = true
                        position = LatLng(latitude, longitude)
                    }

                    // 최초 1회만 현재 위치로 카메라 이동
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

                    if (mode == ViewMode.FISHING) {
                        nearby.forEach { fp ->
                            val tooClose =
                                distanceMeters(latitude, longitude, fp.lat, fp.lon) < 30f
                            if (!tooClose) {
                                val m = Marker().apply {
                                    position = LatLng(fp.lat, fp.lon)
                                    icon = OverlayImage.fromResource(
                                        com.naver.maps.map.R.drawable.navermap_default_marker_icon_green
                                    )
                                    anchor = PointF(0.5f, 1f)
                                    zIndex = 1
                                    setOnClickListener(Overlay.OnClickListener {
                                        onMarkerClick(fp)
                                        true
                                    })
                                    map = nMap
                                }
                                fishingMarkers += m
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

        // 상단: 모드 토글 칩 하나만
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .zIndex(2f)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF2B2B2B))
                .clickable {
                    // 모드 전환 시 선택 초기화 및 안내 노출
                    mode = if (mode == ViewMode.CURRENT) ViewMode.FISHING else ViewMode.CURRENT
                    idx = -1
                    showInfoBox = true
                }
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text(
                text = if (mode == ViewMode.FISHING) "낚시" else "현위치",
                color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold
            )
        }

        // 좌/우 탭 영역 (상/하 100dp는 무시해서 터치 충돌 방지)
        if (mode == ViewMode.FISHING && hasPoints) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f)
                    .pointerInput(hasPoints) {
                        detectTapGestures { offset ->
                            val w = size.width
                            val h = size.height
                            val topGuard = 100.dp.toPx()
                            val bottomGuard = 100.dp.toPx()
                            if (offset.y < topGuard || offset.y > h - bottomGuard) return@detectTapGestures

                            if (offset.x > w * 0.75f) {
                                // 오른쪽: 처음이면 0부터, 아니면 다음
                                idx = if (idx == -1) 0 else (idx + 1) % nearby.size
                                showInfoBox = false
                            } else if (offset.x < w * 0.25f) {
                                // 왼쪽: 처음이면 무시, 아니면 이전
                                if (idx != -1) idx = (idx - 1 + nearby.size) % nearby.size
                                showInfoBox = false
                            }
                        }
                    }
            )
        }

        // 인덱스 바뀌면 선택 포인트로 카메라 이동 (idx >= 0 인 경우에만)
        LaunchedEffect(idx) {
            if (idx >= 0) {
                nearby[idx].let { fp ->
                    naverMapRef?.moveCamera(
                        CameraUpdate.scrollTo(LatLng(fp.lat, fp.lon))
                            .animate(CameraAnimation.Easing)
                    )
                }
            }
        }

        // 하단 카드(이름/거리/인디케이터) — idx 선택된 경우에만 표시
        if (mode == ViewMode.FISHING && hasPoints && idx >= 0) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(), exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .shadow(16.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xF01A1A1A))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentFP?.point_nm ?: "-",
                            color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = currentFP?.point_dt ?: "",
                            color = Color(0xFF58CCFF), fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${idx + 1} / ${nearby.size}",
                            color = Color(0xFFBDBDBD), fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 초기/모드전환 안내 패널 (3초). 포인트 선택되면 숨김.
        AnimatedVisibility(
            visible = showInfoBox && (idx < 0),
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
                            text = if (mode == ViewMode.FISHING) "오른쪽 탭으로 근처 포인트 보기" else region1,
                            fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                        )
                        if (mode == ViewMode.CURRENT && region2.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(text = region2, fontSize = 13.sp, color = Color(0xFFE0E0E0))
                        }
                    }
                }
            }
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
}

private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val out = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, out)
    return out[0]
}
