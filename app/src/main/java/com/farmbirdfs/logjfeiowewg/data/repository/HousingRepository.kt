package com.farmbirdfs.logjfeiowewg.data.repository

import com.farmbirdfs.logjfeiowewg.data.local.dao.ActivityLogDao
import com.farmbirdfs.logjfeiowewg.data.local.dao.HousingDao
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.HousingEntity
import kotlinx.coroutines.flow.Flow

class HousingRepository(
    private val dao: HousingDao,
    private val logDao: ActivityLogDao
) {
    fun getAll(): Flow<List<HousingEntity>> = dao.getAll()
    fun getTotalCount(): Flow<Int> = dao.getTotalCount()

    suspend fun getById(id: Long): HousingEntity? = dao.getById(id)

    suspend fun insert(entity: HousingEntity): Long {
        val id = dao.insert(entity)
        logDao.insert(ActivityLogEntity(action = "Housing Added", details = entity.name, category = "Housing"))
        return id
    }

    suspend fun update(entity: HousingEntity) {
        dao.update(entity)
        logDao.insert(ActivityLogEntity(action = "Housing Updated", details = entity.name, category = "Housing"))
    }

    suspend fun delete(entity: HousingEntity) {
        dao.delete(entity)
        logDao.insert(ActivityLogEntity(action = "Housing Removed", details = entity.name, category = "Housing"))
    }

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
