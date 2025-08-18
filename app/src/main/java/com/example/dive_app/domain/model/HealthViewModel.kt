package com.example.dive_app.domain.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dive_app.data.db.AppDatabase
import com.example.dive_app.data.repository.HealthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: HealthRepository

    // 최근 기록 (그래프용)
    val records: StateFlow<List<HealthRecord>>

    // 현재 심박수 (UI 즉시 반영용)
    private val _currentBpm = MutableStateFlow(0)
    val currentBpm: StateFlow<Int> = _currentBpm

    init {
        // ✅ 싱글톤 DB 인스턴스 사용
        val db = AppDatabase.getDatabase(application)
        repo = HealthRepository(db.healthRecordDao())

        // Flow 수집해서 records로 노출
        records = repo.getAllRecords()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun addHeartRate(bpm: Int) {
        viewModelScope.launch {
            repo.insert(HealthRecord(heartRate = bpm))
            _currentBpm.value = bpm   // 최신 심박수 갱신
        }
    }

    fun clearRecords() {
        viewModelScope.launch {
            repo.clearAll()
            _currentBpm.value = 0
        }
    }
}

