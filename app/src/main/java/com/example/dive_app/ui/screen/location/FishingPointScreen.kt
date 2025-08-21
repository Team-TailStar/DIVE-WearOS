package com.example.dive_app.ui.screen.location

import android.graphics.PointF
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.dive_app.domain.model.FishingPoint
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage

@Composable
fun FishingPointScreen(
    point: FishingPoint,
    navController: NavController,
    fishingPoints: List<FishingPoint>,
    currentLat: Double,
    currentLon: Double,
    onMarkerClick: (FishingPoint) -> Unit = {}
) {
    val mapView = remember { MapView(navController.context) }
    val cameraMoved = remember { mutableStateOf(false) }

    // ★ 우리가 만든 마커들을 보관
    val markers = remember { mutableStateListOf<Marker>() }

    DisposableEffect(mapView) {
        mapView.onStart(); mapView.onResume()
        onDispose {
            // ★ 화면 파괴 시 마커 정리
            markers.forEach { it.map = null }
            markers.clear()
            mapView.onPause(); mapView.onStop(); mapView.onDestroy()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { mv ->
                mv.getMapAsync { naverMap ->
                    naverMap.uiSettings.isZoomControlEnabled = false

                    // 현위치 오버레이
                    with(naverMap.locationOverlay) {
                        isVisible = true
                        position = LatLng(currentLat, currentLon)
                    }

                    // ★ 기존 마커 제거(중복 방지)
                    markers.forEach { it.map = null }
                    markers.clear()

                    // 낚시포인트 마커 생성
                    val eps = 1e-5
                    fishingPoints.forEach { fp ->
                        val isCurrentPos =
                            kotlin.math.abs(fp.lat - currentLat) < eps &&
                                    kotlin.math.abs(fp.lon - currentLon) < eps
                        if (!isCurrentPos) {
                            val m = Marker().apply {
                                position = LatLng(fp.lat, fp.lon)
                                icon = OverlayImage.fromResource(
                                    com.naver.maps.map.R.drawable.navermap_default_marker_icon_green
                                )
                                anchor = PointF(0.5f, 1f)
                                map = naverMap

                                // ★ 클릭 리스너(시그니처 명시)
                                setOnClickListener(Overlay.OnClickListener {
                                    onMarkerClick(fp)
                                    true
                                })
                                // 또는:
                                // setOnClickListener { _: Overlay ->
                                //     onMarkerClick(fp); true
                                // }
                            }
                            markers += m
                        }
                    }

                    // 최초 한 번: 선택 포인트로 카메라 이동
                    if (!cameraMoved.value) {
                        val target = LatLng(point.lat, point.lon)
                        val cu = CameraUpdate.toCameraPosition(
                            com.naver.maps.map.CameraPosition(target, 13.0)
                        )
                        naverMap.moveCamera(cu)
                        cameraMoved.value = true
                    }
                }
            }
        )
    }
}
