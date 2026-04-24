package com.farmbirdfs.logjfeiowewg.data.repository

import com.farmbirdfs.logjfeiowewg.data.local.dao.ActivityLogDao
import com.farmbirdfs.logjfeiowewg.data.local.dao.FeedRecordDao
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.FeedRecordEntity
import kotlinx.coroutines.flow.Flow

class FeedingRepository(
    private val dao: FeedRecordDao,
    private val logDao: ActivityLogDao
) {
    fun getAll(): Flow<List<FeedRecordEntity>> = dao.getAll()
    fun getByGroup(groupId: Long): Flow<List<FeedRecordEntity>> = dao.getByGroup(groupId)
    fun getByDateRange(from: Long, to: Long): Flow<List<FeedRecordEntity>> = dao.getByDateRange(from, to)
    fun getTotalAmountInRange(from: Long, to: Long): Flow<Float?> = dao.getTotalAmountInRange(from, to)

    suspend fun getById(id: Long): FeedRecordEntity? = dao.getById(id)

    suspend fun insert(entity: FeedRecordEntity): Long {
        val id = dao.insert(entity)
        logDao.insert(ActivityLogEntity(
            action = "Feeding Recorded",
            details = "${entity.amount}${entity.unit} of ${entity.feedType}",
            category = "Feeding"
        ))
        return id
    }

    suspend fun update(entity: FeedRecordEntity) = dao.update(entity)
    suspend fun delete(entity: FeedRecordEntity) = dao.delete(entity)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
