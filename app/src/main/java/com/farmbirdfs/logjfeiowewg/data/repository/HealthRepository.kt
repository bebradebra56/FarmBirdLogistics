package com.farmbirdfs.logjfeiowewg.data.repository

import com.farmbirdfs.logjfeiowewg.data.local.dao.ActivityLogDao
import com.farmbirdfs.logjfeiowewg.data.local.dao.HealthRecordDao
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.HealthRecordEntity
import kotlinx.coroutines.flow.Flow

class HealthRepository(
    private val dao: HealthRecordDao,
    private val logDao: ActivityLogDao
) {
    fun getAll(): Flow<List<HealthRecordEntity>> = dao.getAll()
    fun getByGroup(groupId: Long): Flow<List<HealthRecordEntity>> = dao.getByGroup(groupId)
    fun getTotalMortality(): Flow<Int?> = dao.getTotalMortality()
    fun getSickCountSince(since: Long): Flow<Int> = dao.getSickCountSince(since)
    fun getByDateRange(from: Long, to: Long): Flow<List<HealthRecordEntity>> = dao.getByDateRange(from, to)

    suspend fun getById(id: Long): HealthRecordEntity? = dao.getById(id)

    suspend fun insert(entity: HealthRecordEntity): Long {
        val id = dao.insert(entity)
        logDao.insert(ActivityLogEntity(
            action = "Health Check Recorded",
            details = "Condition: ${entity.condition}",
            category = "Health"
        ))
        return id
    }

    suspend fun update(entity: HealthRecordEntity) = dao.update(entity)
    suspend fun delete(entity: HealthRecordEntity) = dao.delete(entity)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
