package com.farmbirdfs.logjfeiowewg.data.repository

import com.farmbirdfs.logjfeiowewg.data.local.dao.ActivityLogDao
import com.farmbirdfs.logjfeiowewg.data.local.dao.BirdGroupDao
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.BirdGroupEntity
import kotlinx.coroutines.flow.Flow

class BirdGroupRepository(
    private val dao: BirdGroupDao,
    private val logDao: ActivityLogDao
) {
    fun getAll(): Flow<List<BirdGroupEntity>> = dao.getAll()
    fun getTotalGroupCount(): Flow<Int> = dao.getTotalGroupCount()
    fun getTotalBirdCount(): Flow<Int?> = dao.getTotalBirdCount()
    fun getByTransport(transportId: Long): Flow<List<BirdGroupEntity>> = dao.getByTransport(transportId)
    fun getByType(type: String): Flow<List<BirdGroupEntity>> = dao.getByType(type)

    suspend fun getById(id: Long): BirdGroupEntity? = dao.getById(id)

    suspend fun insert(entity: BirdGroupEntity): Long {
        val id = dao.insert(entity)
        logDao.insert(ActivityLogEntity(action = "Bird Group Added", details = "${entity.count} ${entity.birdType}s", category = "Birds"))
        return id
    }

    suspend fun update(entity: BirdGroupEntity) {
        dao.update(entity)
        logDao.insert(ActivityLogEntity(action = "Bird Group Updated", details = "${entity.count} ${entity.birdType}s", category = "Birds"))
    }

    suspend fun delete(entity: BirdGroupEntity) {
        dao.delete(entity)
        logDao.insert(ActivityLogEntity(action = "Bird Group Removed", details = entity.birdType, category = "Birds"))
    }

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
