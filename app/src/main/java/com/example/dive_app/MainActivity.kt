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
import com.example.dive_app.domain.viewmodel.AirQualityViewModel
import com.example.dive_app.domain.viewmodel.HealthViewModel
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.domain.viewmodel.TideViewModel
import com.example.dive_app.domain.viewmodel.WeatherViewModel
import com.example.dive_app.sensor.HeartRateSensorManager
import com.example.dive_app.ui.viewmodel.FishingPointViewModel
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import org.json.JSONObject
import kotlin.getValue
import com.example.dive_app.ui.MainApp
/**
 * Wear OS 앱의 메인 Activity
 * - 심박수 센서 데이터 수집
 * - Android(폰)과의 Data Layer 통신 (요청/수신)
 * - ViewModel + Compose UI 연결
 */
class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    // ViewModels
    private val healthViewModel: HealthViewModel by viewModels()
    private val tideViewModel: TideViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val fishViewModel: FishingPointViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()
    private val airQualityViewModel: AirQualityViewModel by viewModels()

    private lateinit var heartRateSensorManager: HeartRateSensorManager
    private lateinit var repo: WearDataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 센서 권한 요청
        requestSensorPermission()
        // 심박수 매니저 초기화
        heartRateSensorManager = HeartRateSensorManager(this) { bpm ->
            Log.d("WatchMsg", "❤️ Heart rate: $bpm BPM")
            healthViewModel.addHeartRate(bpm)

            val responseJson = JSONObject().apply {
                put("heart_rate", bpm)
                put("timestamp", System.currentTimeMillis())
            }
            replyToPhone("/response_heart_rate", responseJson.toString())
        }
        repo = WearDataRepository(
            weatherViewModel, tideViewModel, fishViewModel, locationViewModel, airQualityViewModel)

        setContent {
            MainApp(
                healthViewModel, fishViewModel, weatherViewModel,
                tideViewModel, locationViewModel, airQualityViewModel
            )
        }

    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        val data = String(messageEvent.data, Charsets.UTF_8)

        when (path) {
            "/request_heart_rate" -> {
                Log.d("WatchMsg", "📩 폰에서 심박수 요청 받음")

                val latestBpm = healthViewModel.currentBpm.value

                val responseJson = JSONObject().apply {
                    put("heart_rate", latestBpm)
                    put("timestamp", System.currentTimeMillis())
                }
                replyToPhone("/response_heart_rate", responseJson.toString())
            }
            else -> repo.handleMessage(path, data)
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

    /**
     * 요청 매서드
     */
    fun requestWeather() = replyToPhone("/request_weather", "request")
    fun requestTide() = replyToPhone("/request_tide", "request")
    fun requestPoint() = replyToPhone("/request_point", "request")
    fun requestAirQuality() = replyToPhone("/request_air_quality", "request")
    fun requestLocation() = replyToPhone("/request_location", "request")

    /**
     * 메시지 전송 공통 함수
     */
    private fun replyToPhone(path: String, message: String) {
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                nodes.forEach { node ->
                    Wearable.getMessageClient(this)
                        .sendMessage(node.id, path, message.toByteArray())
                        .addOnSuccessListener {
                            Log.d("WatchMsg", "📨 폰으로 메시지 전송 성공 → $path")
                        }
                        .addOnFailureListener { e ->
                            Log.e("WatchMsg", "⚠️ 메시지 전송 실패: ${e.message}")
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


