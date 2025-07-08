package com.ceo.ticketissuance.domain.model

import java.time.LocalDateTime

data class User(
    val id: Long = 0,
    val username: String,
    val fullName: String?,
    val badgeNumber: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)