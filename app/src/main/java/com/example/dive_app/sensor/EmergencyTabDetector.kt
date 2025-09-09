package com.example.dive_app.sensor

import android.util.Log

class EmergencyTapDetector(
    private val onMultiTap: () -> Unit
) {
    private var tapCount = 0
    private var lastTapTime = 0L

    fun onTapped() {
        val now = System.currentTimeMillis()
        if (now - lastTapTime < 2000) { // 2초 안에 연속 탭만 인정
            tapCount++
            if (tapCount >= 10) {       // ✅ 10회 연속 탭
                onMultiTap()
                tapCount = 0
            }
        } else {
            tapCount = 1
        }
        lastTapTime = now
        Log.d("EmergencyTap", "탭 감지: $tapCount")
    }
}

