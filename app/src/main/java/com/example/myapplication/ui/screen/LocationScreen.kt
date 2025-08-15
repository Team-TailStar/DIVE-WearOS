package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker

@Composable
fun LocationScreen(navController: NavController) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    // 고정 좌표 (예: 부산역)
    val latitude = 35.1151
    val longitude = 129.0415

    DisposableEffect(Unit) {
        mapView.onCreate(null)
        onDispose {
            mapView.onDestroy()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    getMapAsync { naverMap ->
                        naverMap.uiSettings.isZoomControlEnabled = false

                        // 카메라를 고정 좌표로 이동
                        val location = LatLng(latitude, longitude)
                        naverMap.moveCamera(CameraUpdate.scrollTo(location))

                        // 마커 추가
                        Marker().apply {
                            position = location
                            map = naverMap
                        }
                    }
                }
            }
        )

        // 하단 주소 표시
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .background(Color(0xFF212121), RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "부산광역시",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "동구 초량동",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}
