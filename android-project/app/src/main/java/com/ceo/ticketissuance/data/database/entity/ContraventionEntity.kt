package com.ceo.ticketissuance.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(
    tableName = "contraventions",
    foreignKeys = [
        ForeignKey(
            entity = StreetEntity::class,
            parentColumns = ["id"],
            childColumns = ["street_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["street_id"])]
)
data class ContraventionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "code")
    val code: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "observation_time_minutes")
    val observationTimeMinutes: Int,
    
    @ColumnInfo(name = "penalty_amount")
    val penaltyAmount: BigDecimal,
    
    @ColumnInfo(name = "street_id")
    val streetId: Long,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime
)