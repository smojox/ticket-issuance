package com.ceo.ticketissuance.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "vehicle_models",
    foreignKeys = [
        ForeignKey(
            entity = VehicleMakeEntity::class,
            parentColumns = ["id"],
            childColumns = ["make_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["make_id"])]
)
data class VehicleModelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "make_id")
    val makeId: Long,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime
)