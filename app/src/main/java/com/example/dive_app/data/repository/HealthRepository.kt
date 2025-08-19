package com.example.dive_app.data.repository

import com.example.dive_app.data.dao.HealthRecordDao
import com.example.dive_app.domain.model.HealthRecord
import kotlinx.coroutines.flow.Flow

class HealthRepository(private val dao: HealthRecordDao) {

    suspend fun insert(record: HealthRecord) {
        dao.insert(record)
    }

    fun getAllRecords(): Flow<List<HealthRecord>> {
        return dao.getAllRecords()
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
