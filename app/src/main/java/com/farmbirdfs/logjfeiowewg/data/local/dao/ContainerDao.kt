package com.farmbirdfs.logjfeiowewg.data.local.dao

import androidx.room.*
import com.farmbirdfs.logjfeiowewg.data.local.entity.ContainerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {
    @Query("SELECT * FROM containers ORDER BY name ASC")
    fun getAll(): Flow<List<ContainerEntity>>

    @Query("SELECT * FROM containers WHERE id = :id")
    suspend fun getById(id: Long): ContainerEntity?

    @Query("SELECT COUNT(*) FROM containers")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT SUM(currentBirdCount) FROM containers")
    fun getTotalBirdsInContainers(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ContainerEntity): Long

    @Update
    suspend fun update(entity: ContainerEntity)

    @Delete
    suspend fun delete(entity: ContainerEntity)

    @Query("DELETE FROM containers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE containers SET currentBirdCount = currentBirdCount + :count WHERE id = :id")
    suspend fun addBirds(id: Long, count: Int)
}
