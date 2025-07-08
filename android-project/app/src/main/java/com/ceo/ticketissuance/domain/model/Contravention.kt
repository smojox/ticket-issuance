package com.ceo.ticketissuance.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Contravention(
    val id: Long = 0,
    val code: String,
    val description: String,
    val observationTimeMinutes: Int,
    val penaltyAmount: BigDecimal,
    val streetId: Long,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)