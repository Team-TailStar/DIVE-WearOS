package com.example.dive_app.domain.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dive_app.data.db.AppDatabase
import com.example.dive_app.data.repository.HealthRepository
import com.example.dive_app.domain.model.HealthRecord
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
