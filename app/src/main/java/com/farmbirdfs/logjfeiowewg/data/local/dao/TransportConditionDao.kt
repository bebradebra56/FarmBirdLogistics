package com.farmbirdfs.logjfeiowewg.data.local.dao

import androidx.room.*
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportConditionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransportConditionDao {
    @Query("SELECT * FROM transport_conditions WHERE transportPlanId = :transportId ORDER BY timestamp DESC")
    fun getByTransport(transportId: Long): Flow<List<TransportConditionEntity>>

    @Query("SELECT * FROM transport_conditions WHERE transportPlanId = :transportId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestForTransport(transportId: Long): TransportConditionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransportConditionEntity): Long

    @Delete
    suspend fun delete(entity: TransportConditionEntity)

    @Query("DELETE FROM transport_conditions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
