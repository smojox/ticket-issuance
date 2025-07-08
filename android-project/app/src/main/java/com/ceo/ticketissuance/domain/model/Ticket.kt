package com.ceo.ticketissuance.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Ticket(
    val id: Long = 0,
    val ticketNumber: String,
    val vrm: String,
    val streetId: Long,
    val contraventionId: Long,
    val makeId: Long,
    val modelId: Long,
    val color: String,
    val valvePositionFront: Int?,
    val valvePositionRear: Int?,
    val observationId: Long?,
    val issueTime: LocalDateTime,
    val penaltyAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val photoPath: String?,
    val notes: String?,
    val status: TicketStatus,
    val userId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class TicketStatus {
    ISSUED,
    PAID,
    CANCELLED,
    APPEALED
}