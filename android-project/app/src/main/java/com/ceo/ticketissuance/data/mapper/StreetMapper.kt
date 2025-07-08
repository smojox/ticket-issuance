package com.ceo.ticketissuance.data.mapper

import com.ceo.ticketissuance.data.database.entity.StreetEntity
import com.ceo.ticketissuance.domain.model.Street

fun StreetEntity.toDomain(): Street {
    return Street(
        id = id,
        name = name,
        areaCode = areaCode,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Street.toEntity(): StreetEntity {
    return StreetEntity(
        id = id,
        name = name,
        areaCode = areaCode,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun List<StreetEntity>.toDomain(): List<Street> = map { it.toDomain() }
fun List<Street>.toEntity(): List<StreetEntity> = map { it.toEntity() }