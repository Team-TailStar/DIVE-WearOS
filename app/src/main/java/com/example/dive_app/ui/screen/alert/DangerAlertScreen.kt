package com.example.dive_app.ui.screen.alert

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun EmergencyScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    var counter by remember { mutableStateOf(10) }

    // 10초 카운트다운
    LaunchedEffect(Unit) {
        repeat(10) {
            delay(600)
            counter--
        }
        navController.popBackStack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ✅ 제목
            Text(
                text = "위험 감지",
                fontSize = 22.sp,          // 조금 줄인 크기
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ 설명 문구
            Text(
                text = "버튼을 누르면 119로 연결됩니다",
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            // ✅ 설명 문구
            Text(
                text = "6초후 자동 취소",
                fontSize = 12.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ 연결 버튼
            Button(
                onClick = {
                    callEmergency(context)
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "전화",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("연결", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(36.dp)
            ) {
                Text("취소", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** 119 연결 함수 */
fun callEmergency(context: Context) {
    val target = "01095772228"   // 현재는 119 고정

    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:$target")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
        == PackageManager.PERMISSION_GRANTED
    ) {
        context.startActivity(intent)
    } else {
        // 권한 없으면 다이얼 화면까지만
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$target"))
        context.startActivity(dialIntent)
    }
}