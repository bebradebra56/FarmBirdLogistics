package com.farmbirdfs.logjfeiowewg.data.repository

import com.farmbirdfs.logjfeiowewg.data.local.dao.ActivityLogDao
import com.farmbirdfs.logjfeiowewg.data.local.dao.TransportConditionDao
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportConditionEntity
import kotlinx.coroutines.flow.Flow

class ConditionRepository(
    private val dao: TransportConditionDao,
    private val logDao: ActivityLogDao
) {
    fun getByTransport(transportId: Long): Flow<List<TransportConditionEntity>> = dao.getByTransport(transportId)
    suspend fun getLatestForTransport(transportId: Long): TransportConditionEntity? = dao.getLatestForTransport(transportId)

    suspend fun insert(entity: TransportConditionEntity): Long {
        val id = dao.insert(entity)
        logDao.insert(ActivityLogEntity(
            action = "Condition Logged",
            details = "Temp: ${entity.temperature}°, Humidity: ${entity.humidity}%",
            category = "Conditions"
        ))
        return id
    }

    suspend fun delete(entity: TransportConditionEntity) = dao.delete(entity)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
