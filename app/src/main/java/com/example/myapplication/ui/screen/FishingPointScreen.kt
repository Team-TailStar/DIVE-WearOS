package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.myapplication.domain.model.FishingPoint
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker

@Composable
fun FishingPointPage(
    point: FishingPoint,
    navController: NavController,
) {
    val mapView = MapView(LocalContext.current)

    DisposableEffect(Unit) {
        mapView.onCreate(null)
        onDispose { mapView.onDestroy() }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        // 네이버 지도
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    getMapAsync { naverMap ->
                        naverMap.uiSettings.isZoomControlEnabled = false
                        naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(point.lat, point.lon)))
                        Marker().apply {
                            position = LatLng(point.lat, point.lon)
                            map = naverMap
                        }
                    }
                }
            }
        )

        // 왼쪽 화살표
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "이전",
            tint = Color.Black,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(28.dp)
        )

        // 오른쪽 화살표
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "다음",
            tint = Color.Black,
            modifier = Modifier
                .align(Alignment.CenterEnd)
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
            Text("낚시포인트", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        // 하단 정보 박스
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .width(220.dp)
                .background(Color(0xCC212121), RoundedCornerShape(50.dp))
                .padding(10.dp)
                .clickable {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("fishingPoint", point)
                    navController.navigate("fishingDetail")
                }
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = point.point_nm,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = point.point_dt ?: "거리 정보 없음",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Cyan
            )
        }
    }
}
