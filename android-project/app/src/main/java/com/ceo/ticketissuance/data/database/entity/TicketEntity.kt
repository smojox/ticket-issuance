package com.ceo.ticketissuance.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(
            entity = StreetEntity::class,
            parentColumns = ["id"],
            childColumns = ["street_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContraventionEntity::class,
            parentColumns = ["id"],
            childColumns = ["contravention_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VehicleMakeEntity::class,
            parentColumns = ["id"],
            childColumns = ["make_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VehicleModelEntity::class,
            parentColumns = ["id"],
            childColumns = ["model_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ObservationEntity::class,
            parentColumns = ["id"],
            childColumns = ["observation_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ticket_number"], unique = true),
        Index(value = ["vrm"]),
        Index(value = ["street_id"]),
        Index(value = ["contravention_id"]),
        Index(value = ["make_id"]),
        Index(value = ["model_id"]),
        Index(value = ["observation_id"]),
        Index(value = ["user_id"]),
        Index(value = ["status"])
    ]
)
data class TicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "ticket_number")
    val ticketNumber: String,
    
    @ColumnInfo(name = "vrm")
    val vrm: String,
    
    @ColumnInfo(name = "street_id")
    val streetId: Long,
    
    @ColumnInfo(name = "contravention_id")
    val contraventionId: Long,
    
    @ColumnInfo(name = "make_id")
    val makeId: Long,
    
    @ColumnInfo(name = "model_id")
    val modelId: Long,
    
    @ColumnInfo(name = "color")
    val color: String,
    
    @ColumnInfo(name = "valve_position_front")
    val valvePositionFront: Int?,
    
    @ColumnInfo(name = "valve_position_rear")
    val valvePositionRear: Int?,
    
    @ColumnInfo(name = "observation_id")
    val observationId: Long?,
    
    @ColumnInfo(name = "issue_time")
    val issueTime: LocalDateTime,
    
    @ColumnInfo(name = "penalty_amount")
    val penaltyAmount: BigDecimal,
    
    @ColumnInfo(name = "discount_amount")
    val discountAmount: BigDecimal,
    
    @ColumnInfo(name = "photo_path")
    val photoPath: String?,
    
    @ColumnInfo(name = "notes")
    val notes: String?,
    
    @ColumnInfo(name = "status")
    val status: String,
    
    @ColumnInfo(name = "user_id")
    val userId: Long,
    
    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime
)