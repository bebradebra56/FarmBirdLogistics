package com.farmbirdfs.logjfeiowewg.data.repository

import com.farmbirdfs.logjfeiowewg.data.local.dao.ActivityLogDao
import com.farmbirdfs.logjfeiowewg.data.local.dao.ContainerDao
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.ContainerEntity
import kotlinx.coroutines.flow.Flow

class ContainerRepository(
    private val dao: ContainerDao,
    private val logDao: ActivityLogDao
) {
    fun getAll(): Flow<List<ContainerEntity>> = dao.getAll()
    fun getTotalCount(): Flow<Int> = dao.getTotalCount()
    fun getTotalBirdsInContainers(): Flow<Int?> = dao.getTotalBirdsInContainers()

    suspend fun getById(id: Long): ContainerEntity? = dao.getById(id)

    suspend fun insert(entity: ContainerEntity): Long {
        val id = dao.insert(entity)
        logDao.insert(ActivityLogEntity(action = "Container Added", details = entity.name, category = "Containers"))
        return id
    }

    suspend fun update(entity: ContainerEntity) {
        dao.update(entity)
        logDao.insert(ActivityLogEntity(action = "Container Updated", details = entity.name, category = "Containers"))
    }

    suspend fun delete(entity: ContainerEntity) {
        dao.delete(entity)
        logDao.insert(ActivityLogEntity(action = "Container Removed", details = entity.name, category = "Containers"))
    }

    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun addBirds(id: Long, count: Int) = dao.addBirds(id, count)
}
