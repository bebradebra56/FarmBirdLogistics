package com.farmbirdfs.logjfeiowewg.data.repository

import com.farmbirdfs.logjfeiowewg.data.local.dao.ActivityLogDao
import com.farmbirdfs.logjfeiowewg.data.local.dao.TaskDao
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val dao: TaskDao,
    private val logDao: ActivityLogDao
) {
    fun getAll(): Flow<List<TaskEntity>> = dao.getAll()
    fun getPending(): Flow<List<TaskEntity>> = dao.getPending()
    fun getPendingCount(): Flow<Int> = dao.getPendingCount()
    fun getByDateRange(from: Long, to: Long): Flow<List<TaskEntity>> = dao.getByDateRange(from, to)

    suspend fun getById(id: Long): TaskEntity? = dao.getById(id)

    suspend fun insert(entity: TaskEntity): Long {
        val id = dao.insert(entity)
        logDao.insert(ActivityLogEntity(action = "Task Created", details = entity.title, category = "Tasks"))
        return id
    }

    suspend fun update(entity: TaskEntity) = dao.update(entity)

    suspend fun delete(entity: TaskEntity) {
        dao.delete(entity)
        logDao.insert(ActivityLogEntity(action = "Task Deleted", details = entity.title, category = "Tasks"))
    }

    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun setCompleted(id: Long, completed: Boolean) {
        dao.setCompleted(id, completed)
        if (completed) logDao.insert(ActivityLogEntity(action = "Task Completed", details = "Task #$id", category = "Tasks"))
    }
}
