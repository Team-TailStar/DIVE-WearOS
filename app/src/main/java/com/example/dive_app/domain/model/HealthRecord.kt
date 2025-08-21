package com.example.dive_app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_record")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val heartRate: Int,  // 심박수
    val spo2: Int,
    val timestamp: Long = System.currentTimeMillis()
)