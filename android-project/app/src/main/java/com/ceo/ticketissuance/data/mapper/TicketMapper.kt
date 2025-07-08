package com.ceo.ticketissuance.data.mapper

import com.ceo.ticketissuance.data.database.entity.TicketEntity
import com.ceo.ticketissuance.domain.model.Ticket
import com.ceo.ticketissuance.domain.model.TicketStatus

fun TicketEntity.toDomain(): Ticket {
    return Ticket(
        id = id,
        ticketNumber = ticketNumber,
        vrm = vrm,
        streetId = streetId,
        contraventionId = contraventionId,
        makeId = makeId,
        modelId = modelId,
        color = color,
        valvePositionFront = valvePositionFront,
        valvePositionRear = valvePositionRear,
        observationId = observationId,
        issueTime = issueTime,
        penaltyAmount = penaltyAmount,
        discountAmount = discountAmount,
        photoPath = photoPath,
        notes = notes,
        status = TicketStatus.valueOf(status.uppercase()),
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Ticket.toEntity(): TicketEntity {
    return TicketEntity(
        id = id,
        ticketNumber = ticketNumber,
        vrm = vrm,
        streetId = streetId,
        contraventionId = contraventionId,
        makeId = makeId,
        modelId = modelId,
        color = color,
        valvePositionFront = valvePositionFront,
        valvePositionRear = valvePositionRear,
        observationId = observationId,
        issueTime = issueTime,
        penaltyAmount = penaltyAmount,
        discountAmount = discountAmount,
        photoPath = photoPath,
        notes = notes,
        status = status.name.lowercase(),
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun List<TicketEntity>.toDomain(): List<Ticket> = map { it.toDomain() }
fun List<Ticket>.toEntity(): List<TicketEntity> = map { it.toEntity() }