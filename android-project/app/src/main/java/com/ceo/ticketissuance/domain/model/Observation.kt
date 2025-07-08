package com.ceo.ticketissuance.domain.model

import java.time.LocalDateTime

data class Observation(
    val id: Long = 0,
    val vrm: String,
    val streetId: Long,
    val contraventionId: Long,
    val makeId: Long,
    val modelId: Long,
    val valvePositionFront: Int?,
    val valvePositionRear: Int?,
    val observationStartTime: LocalDateTime,
    val observationEndTime: LocalDateTime?,
    val status: ObservationStatus,
    val photoPath: String?,
    val userId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class ObservationStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}