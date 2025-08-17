package com.example.myapplication.domain.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.myapplication.data.db.AppDatabase
import com.example.myapplication.data.repository.HealthRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    // ⚡ db를 클래스 멤버로 선언
    private val db: AppDatabase
    private val repo: HealthRepository

    val records: StateFlow<List<HealthRecord>>

    init {
        db = Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "health_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        repo = HealthRepository(db.healthRecordDao())

        records = repo.getRecords()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        // ⚡ 예시 데이터 삽입 (앱 실행할 때 1번만 실행됨)
        viewModelScope.launch {
            repo.insert(HealthRecord(heartRate = 72))
            repo.insert(HealthRecord(heartRate = 76))
            repo.insert(HealthRecord(heartRate = 68))
            repo.insert(HealthRecord(heartRate = 80))
        }
    }

    fun addHeartRate(bpm: Int) {
        viewModelScope.launch {
            repo.insert(HealthRecord(heartRate = bpm))
        }
    }

    fun clearRecords() {
        viewModelScope.launch {
            db.healthRecordDao().clearAll()
        }
    }
}
