package com.example.dive_app.ui.screen.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dive_app.R
import com.example.dive_app.domain.viewmodel.WeatherViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.dive_app.MainActivity

@Composable
fun SeaWeatherScreen(navController: NavController,  weatherViewModel: WeatherViewModel) {
    val context = LocalContext.current
    val uiState by weatherViewModel.uiState
    LaunchedEffect(Unit) {
        (context as MainActivity).requestWeather()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 🔹 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.sea_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // 화면 꽉 차게
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 수온
            Text(
                text = "수온",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = uiState.obsWt,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .background(Color(0x66FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 파고/파향
            Text(
                text = "파고/파향",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "${uiState.waveHt.ifEmpty { "-" }}m  ${uiState.waveDir.ifEmpty { "-" }}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .background(Color(0x66FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        // 왼쪽 뒤로가기 버튼
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "뒤로가기",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(36.dp)
                .padding(8.dp)
                .clickable { navController.popBackStack() }
        )
    }
}
