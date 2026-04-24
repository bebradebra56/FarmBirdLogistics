package com.farmbirdfs.logjfeiowewg.data.local.dao

import androidx.room.*
import com.farmbirdfs.logjfeiowewg.data.local.entity.HousingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HousingDao {
    @Query("SELECT * FROM housing_units ORDER BY name ASC")
    fun getAll(): Flow<List<HousingEntity>>

    @Query("SELECT * FROM housing_units WHERE id = :id")
    suspend fun getById(id: Long): HousingEntity?

    @Query("SELECT COUNT(*) FROM housing_units")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HousingEntity): Long

    @Update
    suspend fun update(entity: HousingEntity)

    @Delete
    suspend fun delete(entity: HousingEntity)

    @Query("DELETE FROM housing_units WHERE id = :id")
    suspend fun deleteById(id: Long)
}
