package com.farmbirdfs.logjfeiowewg.data.local.dao

import androidx.room.*
import com.farmbirdfs.logjfeiowewg.data.local.entity.LoadingRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoadingRecordDao {
    @Query("SELECT * FROM loading_records WHERE transportPlanId = :transportId ORDER BY loadedAt DESC")
    fun getByTransport(transportId: Long): Flow<List<LoadingRecordEntity>>

    @Query("SELECT * FROM loading_records WHERE containerId = :containerId")
    fun getByContainer(containerId: Long): Flow<List<LoadingRecordEntity>>

    @Query("SELECT SUM(count) FROM loading_records WHERE transportPlanId = :transportId")
    fun getTotalLoadedForTransport(transportId: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LoadingRecordEntity): Long

    @Delete
    suspend fun delete(entity: LoadingRecordEntity)

    @Query("DELETE FROM loading_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
