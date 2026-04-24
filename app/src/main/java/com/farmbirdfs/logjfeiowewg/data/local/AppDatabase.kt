package com.farmbirdfs.logjfeiowewg.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.farmbirdfs.logjfeiowewg.data.local.dao.*
import com.farmbirdfs.logjfeiowewg.data.local.entity.*

@Database(
    entities = [
        TransportPlanEntity::class,
        BirdGroupEntity::class,
        ContainerEntity::class,
        LoadingRecordEntity::class,
        RouteStopEntity::class,
        TransportConditionEntity::class,
        HousingEntity::class,
        FeedRecordEntity::class,
        HealthRecordEntity::class,
        TaskEntity::class,
        InventoryItemEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transportPlanDao(): TransportPlanDao
    abstract fun birdGroupDao(): BirdGroupDao
    abstract fun containerDao(): ContainerDao
    abstract fun loadingRecordDao(): LoadingRecordDao
    abstract fun routeStopDao(): RouteStopDao
    abstract fun transportConditionDao(): TransportConditionDao
    abstract fun housingDao(): HousingDao
    abstract fun feedRecordDao(): FeedRecordDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun taskDao(): TaskDao
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun activityLogDao(): ActivityLogDao
}
