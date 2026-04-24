package com.farmbirdfs.logjfeiowewg.data.repository

import com.farmbirdfs.logjfeiowewg.data.local.dao.ActivityLogDao
import com.farmbirdfs.logjfeiowewg.data.local.dao.TransportPlanDao
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportPlanEntity
import kotlinx.coroutines.flow.Flow

class TransportRepository(
    private val dao: TransportPlanDao,
    private val logDao: ActivityLogDao
) {
    fun getAll(): Flow<List<TransportPlanEntity>> = dao.getAll()
    fun getActiveCount(): Flow<Int> = dao.getActiveCount()
    fun getUpcoming(fromDate: Long): Flow<List<TransportPlanEntity>> = dao.getUpcoming(fromDate)

    suspend fun getById(id: Long): TransportPlanEntity? = dao.getById(id)

    suspend fun insert(entity: TransportPlanEntity): Long {
        val id = dao.insert(entity)
        logDao.insert(ActivityLogEntity(action = "Transport Created", details = entity.name, category = "Transport"))
        return id
    }

    suspend fun update(entity: TransportPlanEntity) {
        dao.update(entity)
        logDao.insert(ActivityLogEntity(action = "Transport Updated", details = entity.name, category = "Transport"))
    }

    suspend fun delete(entity: TransportPlanEntity) {
        dao.delete(entity)
        logDao.insert(ActivityLogEntity(action = "Transport Deleted", details = entity.name, category = "Transport"))
    }

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
