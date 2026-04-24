package com.farmbirdfs.logjfeiowewg.data.local.dao

import androidx.room.*
import com.farmbirdfs.logjfeiowewg.data.local.entity.RouteStopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteStopDao {
    @Query("SELECT * FROM route_stops WHERE transportPlanId = :transportId ORDER BY stopOrder ASC")
    fun getByTransport(transportId: Long): Flow<List<RouteStopEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RouteStopEntity): Long

    @Update
    suspend fun update(entity: RouteStopEntity)

    @Delete
    suspend fun delete(entity: RouteStopEntity)

    @Query("DELETE FROM route_stops WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE route_stops SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)
}
