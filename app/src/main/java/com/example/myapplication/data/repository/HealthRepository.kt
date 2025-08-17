// data/repository/HealthRepository.kt
package com.example.myapplication.data.repository

import androidx.room.Query
import com.example.myapplication.data.dao.HealthRecordDao
import com.example.myapplication.domain.model.HealthRecord
import kotlinx.coroutines.flow.Flow

class HealthRepository(private val dao: HealthRecordDao) {
    suspend fun insert(record: HealthRecord) = dao.insert(record)
    fun getRecords(): Flow<List<HealthRecord>> = dao.getAllRecords()
}

