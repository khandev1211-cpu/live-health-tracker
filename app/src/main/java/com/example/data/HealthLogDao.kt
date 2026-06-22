package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthLogDao {
    @Query("SELECT * FROM health_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<HealthLog>>

    @Query("SELECT * FROM health_logs WHERE type = :type ORDER BY timestamp DESC")
    fun getLogsByType(type: String): Flow<List<HealthLog>>

    @Query("SELECT * FROM health_logs WHERE type IN (:types) ORDER BY timestamp DESC")
    fun getLogsByTypes(types: List<String>): Flow<List<HealthLog>>

    @Query("SELECT * FROM health_logs WHERE type = :type ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogsByType(type: String, limit: Int): List<HealthLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HealthLog): Long

    @Delete
    suspend fun deleteLog(log: HealthLog)

    @Query("DELETE FROM health_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM health_logs")
    suspend fun clearAllLogs()
}
