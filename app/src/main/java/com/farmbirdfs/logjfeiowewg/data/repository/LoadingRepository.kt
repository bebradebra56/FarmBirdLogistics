package com.farmbirdfs.logjfeiowewg.data.repository

import com.farmbirdfs.logjfeiowewg.data.local.dao.ActivityLogDao
import com.farmbirdfs.logjfeiowewg.data.local.dao.LoadingRecordDao
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.LoadingRecordEntity
import kotlinx.coroutines.flow.Flow

class LoadingRepository(
    private val dao: LoadingRecordDao,
    private val logDao: ActivityLogDao
) {
    fun getByTransport(transportId: Long): Flow<List<LoadingRecordEntity>> = dao.getByTransport(transportId)
    fun getByContainer(containerId: Long): Flow<List<LoadingRecordEntity>> = dao.getByContainer(containerId)
    fun getTotalLoadedForTransport(transportId: Long): Flow<Int?> = dao.getTotalLoadedForTransport(transportId)

    suspend fun insert(entity: LoadingRecordEntity): Long {
        val id = dao.insert(entity)
        logDao.insert(ActivityLogEntity(action = "Birds Loaded", details = "${entity.count} birds to container", category = "Loading"))
        return id
    }

    suspend fun delete(entity: LoadingRecordEntity) = dao.delete(entity)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
