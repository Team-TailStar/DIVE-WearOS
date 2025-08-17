package com.example.myapplication.data.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class HeartRateService(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val heartRateSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    var onHeartRateChanged: ((Int) -> Unit)? = null

    fun start() {
        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val bpm = event.values[0].toInt()
            onHeartRateChanged?.invoke(bpm)   // 👉 콜백으로 전달
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
