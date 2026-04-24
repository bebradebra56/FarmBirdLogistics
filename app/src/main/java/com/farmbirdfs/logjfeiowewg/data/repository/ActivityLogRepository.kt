package com.farmbirdfs.logjfeiowewg.data.repository

import com.farmbirdfs.logjfeiowewg.data.local.dao.ActivityLogDao
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

class ActivityLogRepository(private val dao: ActivityLogDao) {
    fun getAll(): Flow<List<ActivityLogEntity>> = dao.getAll()
    fun getRecent(limit: Int = 50): Flow<List<ActivityLogEntity>> = dao.getRecent(limit)
    fun getByDateRange(from: Long, to: Long): Flow<List<ActivityLogEntity>> = dao.getByDateRange(from, to)

    suspend fun log(action: String, details: String = "", category: String = "General") {
        dao.insert(ActivityLogEntity(action = action, details = details, category = category))
        dao.pruneOld()
    }
}
