package com.farmbirdfs.logjfeiowewg.data.local.dao

import androidx.room.*
import com.farmbirdfs.logjfeiowewg.data.local.entity.FeedRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedRecordDao {
    @Query("SELECT * FROM feed_records ORDER BY date DESC")
    fun getAll(): Flow<List<FeedRecordEntity>>

    @Query("SELECT * FROM feed_records WHERE birdGroupId = :groupId ORDER BY date DESC")
    fun getByGroup(groupId: Long): Flow<List<FeedRecordEntity>>

    @Query("SELECT * FROM feed_records WHERE date >= :from AND date <= :to ORDER BY date DESC")
    fun getByDateRange(from: Long, to: Long): Flow<List<FeedRecordEntity>>

    @Query("SELECT SUM(amount) FROM feed_records WHERE date >= :from AND date <= :to")
    fun getTotalAmountInRange(from: Long, to: Long): Flow<Float?>

    @Query("SELECT * FROM feed_records WHERE id = :id")
    suspend fun getById(id: Long): FeedRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FeedRecordEntity): Long

    @Update
    suspend fun update(entity: FeedRecordEntity)

    @Delete
    suspend fun delete(entity: FeedRecordEntity)

    @Query("DELETE FROM feed_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
