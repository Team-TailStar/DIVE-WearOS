package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.util.LocationUtil
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.example.myapplication.R

@Composable
fun CurrentLocationPage() {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var region1 by remember { mutableStateOf("로딩중...") }
    var region2 by remember { mutableStateOf("") }
    val latitude = 35.1151
    val longitude = 129.0415

    DisposableEffect(Unit) {
        mapView.onCreate(null)
        onDispose { mapView.onDestroy() }
    }

    LaunchedEffect(Unit) {
        LocationUtil.fetchAddressFromCoords(latitude, longitude) { r1, r2 ->
            region1 = r1
            region2 = r2
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    getMapAsync { naverMap ->
                        naverMap.uiSettings.isZoomControlEnabled = false
                        naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(latitude, longitude)).animate(CameraAnimation.Easing))

                        Marker().apply {
                            position = LatLng(latitude, longitude)
                            icon = OverlayImage.fromResource(R.drawable.ic_my_location) // 커스텀 아이콘 적용
                            map = naverMap
                        }
                    }
                }
            }
        )

        // 오른쪽 화살표
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "다음",
            tint = Color.Black,
            modifier = Modifier
                .align(Alignment.CenterEnd) // 화면 오른쪽 중앙
                .padding(end = 4.dp)
                .size(28.dp)
        )

        // 상단 제목
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .background(Color(0xCC212121), RoundedCornerShape(50.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("현위치", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .width(200.dp)
                .background(Color(0xCC212121), RoundedCornerShape(50.dp))
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(region1, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(region2, fontSize = 14.sp, color = Color.White)
        }
    }
}
