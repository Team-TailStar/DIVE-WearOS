package com.example.dive_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dive_app.domain.model.HealthRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HealthRecord)

    @Query("SELECT * FROM health_record ORDER BY timestamp ASC")
    fun getAllRecords(): Flow<List<HealthRecord>>

    @Query("DELETE FROM health_record")
    suspend fun clearAll()
}