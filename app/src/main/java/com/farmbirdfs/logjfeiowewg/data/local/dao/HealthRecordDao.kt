package com.farmbirdfs.logjfeiowewg.data.local.dao

import androidx.room.*
import com.farmbirdfs.logjfeiowewg.data.local.entity.HealthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {
    @Query("SELECT * FROM health_records ORDER BY date DESC")
    fun getAll(): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE birdGroupId = :groupId ORDER BY date DESC")
    fun getByGroup(groupId: Long): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE id = :id")
    suspend fun getById(id: Long): HealthRecordEntity?

    @Query("SELECT * FROM health_records WHERE date >= :from AND date <= :to ORDER BY date DESC")
    fun getByDateRange(from: Long, to: Long): Flow<List<HealthRecordEntity>>

    @Query("SELECT SUM(mortality) FROM health_records")
    fun getTotalMortality(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM health_records WHERE condition != 'Healthy' AND date >= :since")
    fun getSickCountSince(since: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HealthRecordEntity): Long

    @Update
    suspend fun update(entity: HealthRecordEntity)

    @Delete
    suspend fun delete(entity: HealthRecordEntity)

    @Query("DELETE FROM health_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
