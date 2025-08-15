package com.example.myapplication.ui.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@Composable
fun LocationScreen(navController: NavController) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var region1 by remember { mutableStateOf("로딩중...") } // 시/도
    var region2 by remember { mutableStateOf("") }         // 시/군/구 + 동
    var showLocationBtn by remember { mutableStateOf(true) }

    // 부산역 좌표
    val latitude = 35.1151
    val longitude = 129.0415

    // 지도 라이프사이클
    DisposableEffect(Unit) {
        mapView.onCreate(null)
        onDispose { mapView.onDestroy() }
    }

    // 역지오코딩 요청
    LaunchedEffect(Unit) {
        fetchAddressFromCoords(latitude, longitude) { r1, r2 ->
            region1 = r1
            region2 = r2
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 지도
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    getMapAsync { naverMap ->
                        naverMap.uiSettings.isZoomControlEnabled = false

                        // 현위치 좌표
                        val location = LatLng(latitude, longitude)

                        // 카메라 이동
                        naverMap.moveCamera(CameraUpdate.scrollTo(location))

                        // 🔹 마커 추가
                        val marker = Marker()
                        marker.position = location
                        marker.map = naverMap
                    }
                }
            }
        )

        // 상단 현위치 버튼
        AnimatedVisibility(
            visible = showLocationBtn,
            exit = fadeOut(animationSpec = tween(durationMillis = 1000)), // 1초 동안 서서히 사라짐
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xCC212121), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "현 위치",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 하단 위치 표시
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .background(
                    color = Color(0xCC212121),
                    shape = RoundedCornerShape(50.dp)
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = region1,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = region2,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )
        }
    }
}

// 카카오 Reverse Geocoding 호출 (두 줄로 파싱)
fun fetchAddressFromCoords(
    lat: Double,
    lon: Double,
    onResult: (String, String) -> Unit
) {
    val apiKey = "6c70d9ab4ca17bdfa047539c7d8ec0a8"
    val url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=$lon&y=$lat"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "KakaoAK $apiKey")
                .build()

            val response = OkHttpClient().newCall(request).execute()
            val body = response.body?.string()

            if (response.isSuccessful && body != null) {
                val json = JSONObject(body)
                val documents = json.getJSONArray("documents")
                if (documents.length() > 0) {
                    val addressObj = documents.getJSONObject(0).getJSONObject("address")
                    val region1 = addressObj.getString("region_1depth_name") // 시/도
                    val region2 =
                        "${addressObj.getString("region_2depth_name")} ${addressObj.getString("region_3depth_name")}" // 시군구 + 동
                    onResult(region1, region2)
                } else {
                    onResult("주소 없음", "")
                }
            } else {
                onResult("HTTP 오류", "")
            }
        } catch (e: Exception) {
            onResult("주소 불러오기 실패", "")
        }
    }
}


