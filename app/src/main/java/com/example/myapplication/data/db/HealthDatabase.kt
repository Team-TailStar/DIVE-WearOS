// data/db/AppDatabase.kt
package com.example.myapplication.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.dao.HealthRecordDao
import com.example.myapplication.domain.model.HealthRecord

@Database(
    entities = [HealthRecord::class],
    version = 2,          // ⚡ 버전 2로 올림
    exportSchema = false  // 스키마 파일 내보내기 안 함 (개발 편의)
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthRecordDao(): HealthRecordDao
}
