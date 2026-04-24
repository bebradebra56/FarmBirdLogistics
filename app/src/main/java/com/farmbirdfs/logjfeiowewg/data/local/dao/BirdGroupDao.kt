package com.farmbirdfs.logjfeiowewg.data.local.dao

import androidx.room.*
import com.farmbirdfs.logjfeiowewg.data.local.entity.BirdGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BirdGroupDao {
    @Query("SELECT * FROM bird_groups ORDER BY createdAt DESC")
    fun getAll(): Flow<List<BirdGroupEntity>>

    @Query("SELECT * FROM bird_groups WHERE transportPlanId = :transportId")
    fun getByTransport(transportId: Long): Flow<List<BirdGroupEntity>>

    @Query("SELECT * FROM bird_groups WHERE id = :id")
    suspend fun getById(id: Long): BirdGroupEntity?

    @Query("SELECT COUNT(*) FROM bird_groups")
    fun getTotalGroupCount(): Flow<Int>

    @Query("SELECT SUM(count) FROM bird_groups")
    fun getTotalBirdCount(): Flow<Int?>

    @Query("SELECT * FROM bird_groups WHERE birdType = :type ORDER BY createdAt DESC")
    fun getByType(type: String): Flow<List<BirdGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BirdGroupEntity): Long

    @Update
    suspend fun update(entity: BirdGroupEntity)

    @Delete
    suspend fun delete(entity: BirdGroupEntity)

    @Query("DELETE FROM bird_groups WHERE id = :id")
    suspend fun deleteById(id: Long)
}
