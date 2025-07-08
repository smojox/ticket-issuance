package com.ceo.ticketissuance.domain.model

import java.time.LocalDateTime

data class Street(
    val id: Long = 0,
    val name: String,
    val areaCode: String?,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)