package com.example.dive_app.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dive_app.R
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.util.LocationUtil
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.shape.CornerSize

@Composable
fun CurrentLocationScreen(
    locationViewModel: LocationViewModel
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    var region1 by remember { mutableStateOf("로딩중...") }
    var region2 by remember { mutableStateOf("") }

    val location by locationViewModel.location.observeAsState()
    val latitude = location?.first ?: 35.1151
    val longitude = location?.second ?: 129.0415

    LaunchedEffect(latitude, longitude) {
        LocationUtil.fetchAddressFromCoords(latitude, longitude) { r1, r2 ->
            region1 = r1
            region2 = r2
        }
    }
    Log.d("WatchMsg", "현재 위치 : $region1 $region2")

    // 원형 워치에서 가장자리가 보이는 문제를 줄이기 위해 전체를 원형으로 한 번 더 클립
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
                mv.getMapAsync { naverMap ->
                    naverMap.uiSettings.isZoomControlEnabled = false
                    naverMap.moveCamera(
                        CameraUpdate.scrollTo(LatLng(latitude, longitude))
                            .animate(CameraAnimation.Easing)
                    )
                    Marker().apply {
                        position = LatLng(latitude, longitude)
                        icon = OverlayImage.fromResource(R.drawable.ic_my_location)
                        map = naverMap
                    }
                }
            }
        )

        // 하단 가독성용 그라데이션 (좀 더 길고 부드럽게)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x33000000),
                            Color(0x99000000)
                        ),
                        startY = 0f,
                        endY = 600f
                    )
                )
        )

        // 상단 칩: “현 위치”
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp), clip = false)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xCC1F1F1F))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "현 위치",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // (옵션) 오른쪽 내비게이션 화살표 — 스샷엔 없어 보이면 주석 처리해도 됨
        /*Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "다음",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
                .size(28.dp)
        )*/

        // 하단 반투명 정보 패널 (위는 살짝, 아래는 크게 둥글게)
        val bubbleShape = RoundedCornerShape(
            topStartPercent = 60,   // 위를 살짝만
            topEndPercent = 60,
            bottomStartPercent = 100,
            bottomEndPercent = 100
        )

        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.68f)    // 폭 비율 조절
                .padding(bottom = 13.dp) // 원 하단과 거의 맞닿게
        ) {
            val w = maxWidth
            val h = w / 2   // 정확히 반원 높이

            val shape = RoundedCornerShape(
                topStart = 30.dp,
                topEnd   = 30.dp,
                bottomStart = h,
                bottomEnd   = h
            )

            Box(
                modifier = Modifier
                    .width(w)
                    .height(h)
                    .shadow(14.dp, shape)
                    .clip(shape)
                    .background(Color(0xF01A1A1A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = region1,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFF4F4F4)
                    )
                    if (region2.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = region2,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE0E0E0)
                        )
                    }
                }
            }
        }

    }
}
