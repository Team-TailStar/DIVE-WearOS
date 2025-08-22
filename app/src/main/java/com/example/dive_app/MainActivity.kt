package com.example.dive_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dive_app.data.repository.WearDataRepository
import com.example.dive_app.domain.viewmodel.*
import com.example.dive_app.sensor.EmergencyTapDetector
import com.example.dive_app.sensor.HeartRateSensorManager
import com.example.dive_app.sensor.Spo2Manager
import com.example.dive_app.ui.viewmodel.FishingPointViewModel
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import org.json.JSONObject


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
    private lateinit var spo2Manager: Spo2Manager
    private lateinit var tapDetector: EmergencyTapDetector
    private lateinit var repo: WearDataRepository

    private val CHANNEL_ID = "alert_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 권한 요청
        requestSensorPermission()
        requestAlertPermission()
        //requestCallPhonePermission()

        // 알림 채널 생성
        createNotificationChannel()

        // 심박수 매니저 초기화
        heartRateSensorManager = HeartRateSensorManager(this) { bpm ->
            Log.d("WatchMsg", "❤️ Heart rate: $bpm BPM")
            healthViewModel.updateBpm(bpm)

            val responseJson = JSONObject().apply {
                put("heart_rate", bpm)
                put("timestamp", System.currentTimeMillis())
            }
            replyToPhone("/response_heart_rate", responseJson.toString())
        }

        // 긴급탭 감지기
        tapDetector = EmergencyTapDetector {
            healthViewModel.triggerTapEmergency()
        }

        spo2Manager = Spo2Manager(this)
        repo = WearDataRepository(
            weatherViewModel, tideViewModel, fishViewModel, locationViewModel, airQualityViewModel
        )

        setContent {
            MainApp(
                healthViewModel,
                fishViewModel,
                weatherViewModel,
                tideViewModel,
                locationViewModel,
                airQualityViewModel
            )
        }

        // SpO₂ 수집
        lifecycleScope.launch {
            spo2Manager.currentSpo2.collect { spo2 ->
                if (spo2 > 0) {
                    healthViewModel.updateSpo2(spo2)

                    val responseJson = JSONObject().apply {
                        put("spo2", spo2)
                        put("timestamp", System.currentTimeMillis())
                    }
                    replyToPhone("/response_spo2", responseJson.toString())

                    Log.d("WatchMsg", "🩸 SpO₂ 업데이트: $spo2%")
                }
            }
        }

        // 테스트 알림 (앱 실행 시 바로 표시)
        showWatchNotification("테스트 알림", "워치 알림이 정상 동작합니다")
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            tapDetector.onTapped()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        val data = String(messageEvent.data, Charsets.UTF_8)

        when (path) {
            "/typhoon_alert" -> {
                val msg = String(messageEvent.data)
                try {
                    val json = JSONObject(msg)
                    val typhoon = json.getString("typhoon")
                    val distance = json.getDouble("distance")
                    val body = "$typhoon 접근: 약 ${"%.1f".format(distance)} km"

                    showWatchNotification("태풍 경고", body)
                } catch (e: Exception) {
                    showWatchNotification("알림 오류", msg)
                }
            }
            "/weather_alert" -> {
                try {
                    val json = JSONObject(data)
                    val msg = json.getString("weather_alert")
                    showWatchNotification("기상 경고", msg)
                } catch (e: Exception) {
                    showWatchNotification("알림 오류", data)
                }
            }

            "/tide_alert" -> {
                try {
                    val json = JSONObject(data)
                    val msg = json.getString("tide_alert")
                    showWatchNotification("물때 경고", msg)
                } catch (e: Exception) {
                    showWatchNotification("알림 오류", data)
                }
            }
            "/alert_accident" -> {
                try {
                    val json = JSONObject(data)
                    val msg = json.optString("message", "위험지역 경고 발생")
                    val region = json.optString("region", "")
                    val place = json.optString("place_se", "")

                    // 원하는 형태로 포맷
                    val body = if (region.isNotBlank() && place.isNotBlank()) {
                        "[$region $place] $msg"
                    } else {
                        msg
                    }

                    showWatchNotification("위험지역 경고", body)
                } catch (e: Exception) {
                    showWatchNotification("알림 오류", data)
                }
            }
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

    /** 알림 채널 생성 */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "경고 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "각종 경고 알림 채널"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /** 알림 표시 */
    private fun showWatchNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 없으면 그냥 리턴 (또는 요청 로직 추가)
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
                return
            }
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    /** 권한 요청들 */
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

    private fun requestCallPhonePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CALL_PHONE),
                100
            )
        }
    }

    private fun requestAlertPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }

    /** 폰으로 요청 메시지 보내기 */
    fun requestWeather() = replyToPhone("/request_weather", "request")
    fun requestTide() = replyToPhone("/request_tide", "request")
    fun requestPoint() = replyToPhone("/request_point", "request")
    fun requestAirQuality() = replyToPhone("/request_air_quality", "request")
    fun requestLocation() = replyToPhone("/request_location", "request")

    /** 메시지 전송 공통 함수 */
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
        spo2Manager.startListening()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        heartRateSensorManager.stop()
        spo2Manager.stopListening()
        Wearable.getMessageClient(this).removeListener(this)
    }
}


