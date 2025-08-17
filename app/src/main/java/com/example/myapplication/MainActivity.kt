package com.example.myapplication

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.domain.model.HealthViewModel
import com.example.myapplication.sensor.HeartRateSensorManager
import android.hardware.Sensor
import android.hardware.SensorManager   // ✅ 추가
import androidx.activity.viewModels

/**
 * 앱의 진입점(Activity).
 * onCreate에서 Compose 트리를 시작하며, 루트 Composable(MainApp)을 렌더링합니다.
 */
class MainActivity : ComponentActivity() {
    private lateinit var heartRateSensorManager: HeartRateSensorManager
    private val healthViewModel: HealthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestSensorPermission()


        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        sensors.forEach {
            println("📡 Sensor: ${it.name} (type=${it.type})")
        }
        heartRateSensorManager = HeartRateSensorManager(this) { bpm ->
            // 심박수 값 들어옴 → DB 저장하거나 로그 찍기
            println("❤️ Heart rate: $bpm BPM")
            healthViewModel.addHeartRate(bpm)
        }

        setContent {
            MainApp(vm = healthViewModel)
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

    override fun onResume() {
        super.onResume()
        heartRateSensorManager.start()
    }

    override fun onPause() {
        super.onPause()
        heartRateSensorManager.stop()
    }

}

