package com.example.dive_app

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dive_app.data.repository.WearDataRepository
import com.example.dive_app.domain.model.HealthViewModel
import com.example.dive_app.domain.model.TideViewModel
import com.example.dive_app.domain.model.WeatherData
import com.example.dive_app.domain.model.WeatherViewModel
import com.example.dive_app.sensor.HeartRateSensorManager
import com.example.dive_app.ui.viewmodel.FishingPointViewModel
import com.google.android.gms.wearable.Wearable

/**
 * Wear OS 앱의 메인 Activity
 * - 심박수 센서 데이터 수집
 * - Android(폰)과의 Data Layer 통신 (요청/수신)
 * - ViewModel + Compose UI 연결
 */
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import org.json.JSONObject
import kotlin.getValue

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    private lateinit var heartRateSensorManager: HeartRateSensorManager
    private val healthViewModel: HealthViewModel by viewModels()
    private val tideViewModel: TideViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val fishViewModel: FishingPointViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestSensorPermission()

        heartRateSensorManager = HeartRateSensorManager(this) { bpm ->
            println("❤️ Heart rate: $bpm BPM")
            healthViewModel.addHeartRate(bpm)
        }
        val repo = WearDataRepository(this, fishViewModel, weatherViewModel, tideViewModel)

        setContent {
            MainApp(healthViewModel, fishViewModel, weatherViewModel, tideViewModel, repo)
        }
    }

    private fun requestSensorPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BODY_SENSORS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.BODY_SENSORS),
                100
            )
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        val data = String(messageEvent.data)
        val jsonStr = String(messageEvent.data, Charsets.UTF_8)
        val json = JSONObject(jsonStr)

        when (path) {
            "/request_heart_rate" -> {
                Log.d("WatchMsg", "📩 폰에서 심박수 요청 받음:, $data")
                val responseJson = JSONObject().apply {
                    put("heart_rate", "72")  // 실제 센서 데이터 넣기
                    put("timestamp", System.currentTimeMillis())
                }
                replyToPhone("/response_heart_rate", responseJson.toString())
            }
            "/response_weather" -> {
                Log.d("WatchMsg", "📩 날씨 응답 수신: $data")

                val json = JSONObject(data)
                val weather = WeatherData(
                    sky = json.getString("sky"),
                    temp = json.getString("temp"),
                    humidity = json.getString("humidity"),
                    windspd = json.getString("windspd"),
                    rain = json.getString("rain"),
                    winddir = json.getString("winddir"),
                    waveHt = json.getString("waveHt"),
                    waveDir = json.getString("waveDir")
                )

                // ✅ ViewModel에 반영 → UI 업데이트
                weatherViewModel.updateWeather(weather)
            }
            "/response_tide" -> {
                Log.d("WatchMsg", "📩 조석 응답 수신: $data")
            }
            "/response_point" -> {
                Log.d("WatchMsg", "📩 포인트 응답 수신: $data")
            }
            else -> {
                Log.d("WatchMsg", "📩 알 수 없는 응답 → path=$path, data=$data")
            }
        }
    }

    // ✅ 워치 → 폰 응답 공통 함수
    fun replyToPhone(path: String, message: String) {
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                nodes.forEach { node ->
                    Wearable.getMessageClient(this)
                        .sendMessage(node.id, path, message.toByteArray())
                        .addOnSuccessListener {
                            Log.d("WatchMsg", "📨 폰으로 응답 전송 성공 → $path")
                        }
                        .addOnFailureListener { e ->
                            Log.e("WatchMsg", "⚠️ 응답 전송 실패: ${e.message}")
                        }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        heartRateSensorManager.start()
        Wearable.getMessageClient(this).addListener(this)

    }

    override fun onPause() {
        super.onPause()
        heartRateSensorManager.stop()
        Wearable.getMessageClient(this).removeListener(this)

    }

    override fun onDestroy() {
        super.onDestroy()
    }
}


