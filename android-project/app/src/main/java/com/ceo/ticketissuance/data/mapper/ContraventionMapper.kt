package com.ceo.ticketissuance.data.mapper

import com.ceo.ticketissuance.data.database.entity.ContraventionEntity
import com.ceo.ticketissuance.domain.model.Contravention

fun ContraventionEntity.toDomain(): Contravention {
    return Contravention(
        id = id,
        code = code,
        description = description,
        observationTimeMinutes = observationTimeMinutes,
        penaltyAmount = penaltyAmount,
        streetId = streetId,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Contravention.toEntity(): ContraventionEntity {
    return ContraventionEntity(
        id = id,
        code = code,
        description = description,
        observationTimeMinutes = observationTimeMinutes,
        penaltyAmount = penaltyAmount,
        streetId = streetId,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun List<ContraventionEntity>.toDomain(): List<Contravention> = map { it.toDomain() }
fun List<Contravention>.toEntity(): List<ContraventionEntity> = map { it.toEntity() }