package com.example.dive_app.domain.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dive_app.data.db.AppDatabase
import com.example.dive_app.data.repository.HealthRepository
import com.example.dive_app.domain.model.HealthRecord
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// 응급 이벤트 종류
sealed class EmergencyEvent {
    data class HeartRateLow(val bpm: Int) : EmergencyEvent()
    data class Spo2Low(val spo2: Int) : EmergencyEvent()
    object ScreenTapped  : EmergencyEvent()
}

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: HealthRepository

    // 최근 기록 (그래프/리스트)
    val records: StateFlow<List<HealthRecord>>

    // 실시간 값 (UI 반영용)
    private val _currentBpm = MutableStateFlow(0)
    val currentBpm: StateFlow<Int> = _currentBpm

    private val _currentSpo2 = MutableStateFlow(0)
    val currentSpo2: StateFlow<Int> = _currentSpo2

    // 응급 이벤트 스트림
    private val _emergencyEvent = MutableSharedFlow<EmergencyEvent>()
    val emergencyEvent: SharedFlow<EmergencyEvent> = _emergencyEvent

    companion object {
        private const val MIN_BPM = 40    // 심박수 최소 허용
        private const val MIN_SPO2 = 90   // SpO₂ 최소 허용
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repo = HealthRepository(db.healthRecordDao())

        records = repo.getAllRecords()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    private var mockJob: Job? = null

    /** 실시간 Mock 스트리밍 시작 (기본 3초 주기) */
    fun startMockStream(periodMs: Long = 3000L, occasionalAlert: Boolean = false) {
        if (mockJob != null) return  // 중복 시작 방지
        mockJob = viewModelScope.launch {
            var t = 0.0
            val r = Random(System.currentTimeMillis())
            while (isActive) {
                t += 0.5

                // 심박수: 75±(사인파 6 + 잡음 0~3)
                var bpm = (78 + (sin(t) * 6).roundToInt() + r.nextInt(-3, 4))
                    .coerceIn(55, 115)

                // SpO2: 97±1
                var spo2 = (97 + r.nextInt(-1, 2)).coerceIn(92, 100)

                // 가끔 경보 테스트(옵션)
                if (occasionalAlert && r.nextDouble() < 0.03) bpm = r.nextInt(36, 40)   // 저심박
                if (occasionalAlert && r.nextDouble() < 0.02) spo2 = r.nextInt(86, 90)  // 저산소

                addHealthData(bpm, spo2)
                delay(periodMs)
            }
        }
    }

    /** 실시간 Mock 스트리밍 중지 */
    fun stopMockStream() {
        mockJob?.cancel()
        mockJob = null
    }

    /** 과거 기록 시드(그래프 테스트용) */
    fun seedMockHistory(count: Int = 30) {
        viewModelScope.launch {
            val r = Random(System.currentTimeMillis())
            repeat(count) { i ->
                val theta = i / 6.0
                val bpm = (76 + (sin(theta) * 6).roundToInt() + r.nextInt(-2, 3))
                    .coerceIn(55, 115)
                val spo2 = (97 + r.nextInt(-1, 2)).coerceIn(92, 100)
                addHealthData(bpm, spo2)
                delay(30)  // 너무 빨리 넣지 않게 살짝 지연
            }
        }
    }

    /** 심박수 업데이트 */
    fun updateBpm(bpm: Int) {
        addHealthData(bpm, _currentSpo2.value)
    }

    /** 산소포화도 업데이트 */
    fun updateSpo2(spo2: Int) {
        addHealthData(_currentBpm.value, spo2)
    }

    /** 화면 다중 탭 이벤트 */
    fun triggerTapEmergency() {
        viewModelScope.launch {
            Log.d("WatchMsg", "🚨 화면 탭으로 위급 알림 발생")
            _emergencyEvent.emit(EmergencyEvent.ScreenTapped)
        }
    }

    /** 데이터 저장 + 응급 상황 체크 */
    private fun addHealthData(bpm: Int, spo2: Int) {
        viewModelScope.launch {
            val record = HealthRecord(
                heartRate = bpm,
                spo2 = spo2
            )
            repo.insert(record)

            _currentBpm.value = bpm
            _currentSpo2.value = spo2

            checkBpmEmergency(bpm)
            checkSpo2Emergency(spo2)
        }
    }

    private fun checkBpmEmergency(bpm: Int) {
        if (bpm in 1 until MIN_BPM) {
            viewModelScope.launch {
                Log.d("WatchMsg", "🚨 심박수 위험: $bpm BPM")
                _emergencyEvent.emit(EmergencyEvent.HeartRateLow(bpm))
            }
        }
    }

    private fun checkSpo2Emergency(spo2: Int) {
        if (spo2 in 1 until MIN_SPO2) {
            viewModelScope.launch {
                Log.d("WatchMsg", "🚨 SpO₂ 위험: $spo2%")
                _emergencyEvent.emit(EmergencyEvent.Spo2Low(spo2))
            }
        }
    }

    /** DB 전체 초기화 */
    fun clearRecords() {
        viewModelScope.launch {
            repo.clearAll()
            _currentBpm.value = 0
            _currentSpo2.value = 0
        }
    }
}
