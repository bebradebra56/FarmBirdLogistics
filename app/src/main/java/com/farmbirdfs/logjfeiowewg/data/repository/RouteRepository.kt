package com.farmbirdfs.logjfeiowewg.data.repository

import com.farmbirdfs.logjfeiowewg.data.local.dao.ActivityLogDao
import com.farmbirdfs.logjfeiowewg.data.local.dao.RouteStopDao
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.RouteStopEntity
import kotlinx.coroutines.flow.Flow

class RouteRepository(
    private val dao: RouteStopDao,
    private val logDao: ActivityLogDao
) {
    fun getByTransport(transportId: Long): Flow<List<RouteStopEntity>> = dao.getByTransport(transportId)

    suspend fun insert(entity: RouteStopEntity): Long {
        val id = dao.insert(entity)
        logDao.insert(ActivityLogEntity(action = "Route Stop Added", details = entity.location, category = "Route"))
        return id
    }

    suspend fun update(entity: RouteStopEntity) = dao.update(entity)
    suspend fun delete(entity: RouteStopEntity) = dao.delete(entity)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun setCompleted(id: Long, completed: Boolean) = dao.setCompleted(id, completed)
}
