package com.ceo.ticketissuance.domain.model

import java.time.LocalDateTime

data class VehicleModel(
    val id: Long = 0,
    val name: String,
    val makeId: Long,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)