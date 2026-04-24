package com.farmbirdfs.logjfeiowewg.data.local.dao

import androidx.room.*
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE timestamp >= :from AND timestamp <= :to ORDER BY timestamp DESC")
    fun getByDateRange(from: Long, to: Long): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ActivityLogEntity): Long

    @Query("DELETE FROM activity_logs WHERE id NOT IN (SELECT id FROM activity_logs ORDER BY timestamp DESC LIMIT 200)")
    suspend fun pruneOld()
}
