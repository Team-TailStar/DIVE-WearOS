package com.example.dive_app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Spo2Manager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // ⚠️ API 31 이상에서만 지원
    private val spo2Sensor: Sensor? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            sensorManager.getDefaultSensor(CustomSensors.TYPE_OXYGEN_SATURATION)
        } else {
            null
        }

    private val _currentSpo2 = MutableStateFlow(0)
    val currentSpo2: StateFlow<Int> = _currentSpo2

    fun startListening() {
        spo2Sensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } ?: Log.w("Spo2Manager", "❌ SpO2 센서를 지원하지 않거나 접근 불가")
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            event.sensor.type == CustomSensors.TYPE_OXYGEN_SATURATION
        ) {
            val spo2 = event.values[0].toInt()
            _currentSpo2.value = spo2
            Log.d("Spo2Manager", "SpO2: $spo2%")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
