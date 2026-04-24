package com.farmbirdfs.logjfeiowewg.data.local.dao

import androidx.room.*
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransportPlanDao {
    @Query("SELECT * FROM transport_plans ORDER BY date DESC")
    fun getAll(): Flow<List<TransportPlanEntity>>

    @Query("SELECT * FROM transport_plans WHERE status = :status ORDER BY date DESC")
    fun getByStatus(status: String): Flow<List<TransportPlanEntity>>

    @Query("SELECT * FROM transport_plans WHERE id = :id")
    suspend fun getById(id: Long): TransportPlanEntity?

    @Query("SELECT COUNT(*) FROM transport_plans WHERE status = 'IN_PROGRESS'")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT * FROM transport_plans WHERE date >= :fromDate ORDER BY date ASC")
    fun getUpcoming(fromDate: Long): Flow<List<TransportPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransportPlanEntity): Long

    @Update
    suspend fun update(entity: TransportPlanEntity)

    @Delete
    suspend fun delete(entity: TransportPlanEntity)

    @Query("DELETE FROM transport_plans WHERE id = :id")
    suspend fun deleteById(id: Long)
}
