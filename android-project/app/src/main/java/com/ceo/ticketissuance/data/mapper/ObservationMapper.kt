package com.ceo.ticketissuance.data.mapper

import com.ceo.ticketissuance.data.database.entity.ObservationEntity
import com.ceo.ticketissuance.domain.model.Observation
import com.ceo.ticketissuance.domain.model.ObservationStatus

fun ObservationEntity.toDomain(): Observation {
    return Observation(
        id = id,
        vrm = vrm,
        streetId = streetId,
        contraventionId = contraventionId,
        makeId = makeId,
        modelId = modelId,
        valvePositionFront = valvePositionFront,
        valvePositionRear = valvePositionRear,
        observationStartTime = observationStartTime,
        observationEndTime = observationEndTime,
        status = ObservationStatus.valueOf(status.uppercase()),
        photoPath = photoPath,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Observation.toEntity(): ObservationEntity {
    return ObservationEntity(
        id = id,
        vrm = vrm,
        streetId = streetId,
        contraventionId = contraventionId,
        makeId = makeId,
        modelId = modelId,
        valvePositionFront = valvePositionFront,
        valvePositionRear = valvePositionRear,
        observationStartTime = observationStartTime,
        observationEndTime = observationEndTime,
        status = status.name.lowercase(),
        photoPath = photoPath,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun List<ObservationEntity>.toDomain(): List<Observation> = map { it.toDomain() }
fun List<Observation>.toEntity(): List<ObservationEntity> = map { it.toEntity() }