package com.farmbirdfs.logjfeiowewg.data.repository

import com.farmbirdfs.logjfeiowewg.data.local.dao.ActivityLogDao
import com.farmbirdfs.logjfeiowewg.data.local.dao.InventoryItemDao
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val dao: InventoryItemDao,
    private val logDao: ActivityLogDao
) {
    fun getAll(): Flow<List<InventoryItemEntity>> = dao.getAll()
    fun getByCategory(category: String): Flow<List<InventoryItemEntity>> = dao.getByCategory(category)
    fun getLowStock(): Flow<List<InventoryItemEntity>> = dao.getLowStock()

    suspend fun getById(id: Long): InventoryItemEntity? = dao.getById(id)

    suspend fun insert(entity: InventoryItemEntity): Long {
        val id = dao.insert(entity)
        logDao.insert(ActivityLogEntity(action = "Inventory Added", details = entity.name, category = "Inventory"))
        return id
    }

    suspend fun update(entity: InventoryItemEntity) {
        dao.update(entity)
        logDao.insert(ActivityLogEntity(action = "Inventory Updated", details = entity.name, category = "Inventory"))
    }

    suspend fun delete(entity: InventoryItemEntity) {
        dao.delete(entity)
        logDao.insert(ActivityLogEntity(action = "Inventory Removed", details = entity.name, category = "Inventory"))
    }

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
