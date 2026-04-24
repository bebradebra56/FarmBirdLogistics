package com.farmbirdfs.logjfeiowewg.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "transport_plans")
data class TransportPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: Long,
    val origin: String,
    val destination: String,
    val status: String = TransportStatus.PLANNED,
    val birdCount: Int = 0,
    val notes: String = ""
)

object TransportStatus {
    const val PLANNED = "PLANNED"
    const val IN_PROGRESS = "IN_PROGRESS"
    const val COMPLETED = "COMPLETED"
    const val CANCELLED = "CANCELLED"
}

@Entity(tableName = "bird_groups")
data class BirdGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val birdType: String,
    val count: Int,
    val age: String,
    val notes: String = "",
    val transportPlanId: Long? = null,
    val housingId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

object BirdType {
    const val CHICKEN = "Chicken"
    const val DUCK = "Duck"
    const val GOOSE = "Goose"
    const val QUAIL = "Quail"
    const val TURKEY = "Turkey"
    const val OTHER = "Other"
    val all = listOf(CHICKEN, DUCK, GOOSE, QUAIL, TURKEY, OTHER)
}

@Entity(tableName = "containers")
data class ContainerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val capacity: Int,
    val type: String,
    val currentBirdCount: Int = 0,
    val notes: String = ""
)

object ContainerType {
    const val CAGE = "Cage"
    const val CRATE = "Crate"
    const val BOX = "Box"
    const val BASKET = "Basket"
    val all = listOf(CAGE, CRATE, BOX, BASKET)
}

@Entity(
    tableName = "loading_records",
    foreignKeys = [
        ForeignKey(
            entity = TransportPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["transportPlanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BirdGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["birdGroupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContainerEntity::class,
            parentColumns = ["id"],
            childColumns = ["containerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("transportPlanId"),
        Index("birdGroupId"),
        Index("containerId")
    ]
)
data class LoadingRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transportPlanId: Long,
    val birdGroupId: Long,
    val containerId: Long,
    val count: Int,
    val loadedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "route_stops",
    foreignKeys = [
        ForeignKey(
            entity = TransportPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["transportPlanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transportPlanId")]
)
data class RouteStopEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transportPlanId: Long,
    val location: String,
    val stopOrder: Int,
    val isCompleted: Boolean = false,
    val estimatedTime: String = "",
    val notes: String = ""
)

@Entity(
    tableName = "transport_conditions",
    foreignKeys = [
        ForeignKey(
            entity = TransportPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["transportPlanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transportPlanId")]
)
data class TransportConditionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transportPlanId: Long,
    val temperature: Float,
    val humidity: Float,
    val ventilation: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

object VentilationLevel {
    const val GOOD = "Good"
    const val FAIR = "Fair"
    const val POOR = "Poor"
    val all = listOf(GOOD, FAIR, POOR)
}

@Entity(tableName = "housing_units")
data class HousingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val capacity: Int,
    val type: String,
    val currentCount: Int = 0,
    val notes: String = ""
)

object HousingType {
    const val COOP = "Coop"
    const val OUTDOOR_PEN = "Outdoor Pen"
    const val BARN = "Barn"
    const val CAGE_ROOM = "Cage Room"
    val all = listOf(COOP, OUTDOOR_PEN, BARN, CAGE_ROOM)
}

@Entity(
    tableName = "feed_records",
    foreignKeys = [
        ForeignKey(
            entity = BirdGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["birdGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("birdGroupId")]
)
data class FeedRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val birdGroupId: Long,
    val feedType: String,
    val amount: Float,
    val unit: String = "kg",
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)

object FeedType {
    const val GRAIN = "Grain"
    const val PELLETS = "Pellets"
    const val MASH = "Mash"
    const val SCRATCH = "Scratch"
    const val GREENS = "Greens"
    const val MIXED = "Mixed Feed"
    val all = listOf(GRAIN, PELLETS, MASH, SCRATCH, GREENS, MIXED)
}

@Entity(
    tableName = "health_records",
    foreignKeys = [
        ForeignKey(
            entity = BirdGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["birdGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("birdGroupId")]
)
data class HealthRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val birdGroupId: Long,
    val condition: String,
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val treatment: String = "",
    val mortality: Int = 0
)

object HealthCondition {
    const val HEALTHY = "Healthy"
    const val SICK = "Sick"
    const val INJURED = "Injured"
    const val UNDER_TREATMENT = "Under Treatment"
    const val RECOVERED = "Recovered"
    const val DECEASED = "Deceased"
    val all = listOf(HEALTHY, SICK, INJURED, UNDER_TREATMENT, RECOVERED, DECEASED)
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = TaskCategory.OTHER,
    val date: Long,
    val isCompleted: Boolean = false,
    val notes: String = ""
)

object TaskCategory {
    const val TRANSPORT = "Transport"
    const val FEEDING = "Feeding"
    const val HEALTH = "Health"
    const val CLEANING = "Cleaning"
    const val OTHER = "Other"
    val all = listOf(TRANSPORT, FEEDING, HEALTH, CLEANING, OTHER)
}

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val quantity: Int,
    val unit: String = "pcs",
    val minStock: Int = 0,
    val notes: String = ""
)

object InventoryCategory {
    const val CONTAINER = "Container"
    const val FEEDER = "Feeder"
    const val WATER_SYSTEM = "Water System"
    const val FEED = "Feed"
    const val MEDICINE = "Medicine"
    const val BEDDING = "Bedding"
    const val EQUIPMENT = "Equipment"
    const val OTHER = "Other"
    val all = listOf(CONTAINER, FEEDER, WATER_SYSTEM, FEED, MEDICINE, BEDDING, EQUIPMENT, OTHER)
}

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val details: String = "",
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)
