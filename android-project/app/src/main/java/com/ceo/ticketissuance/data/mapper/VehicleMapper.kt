package com.ceo.ticketissuance.data.mapper

import com.ceo.ticketissuance.data.database.entity.VehicleMakeEntity
import com.ceo.ticketissuance.data.database.entity.VehicleModelEntity
import com.ceo.ticketissuance.domain.model.VehicleMake
import com.ceo.ticketissuance.domain.model.VehicleModel

// Vehicle Make Mappers
fun VehicleMakeEntity.toDomain(): VehicleMake {
    return VehicleMake(
        id = id,
        name = name,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun VehicleMake.toEntity(): VehicleMakeEntity {
    return VehicleMakeEntity(
        id = id,
        name = name,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Vehicle Model Mappers
fun VehicleModelEntity.toDomain(): VehicleModel {
    return VehicleModel(
        id = id,
        name = name,
        makeId = makeId,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun VehicleModel.toEntity(): VehicleModelEntity {
    return VehicleModelEntity(
        id = id,
        name = name,
        makeId = makeId,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// List Extensions
fun List<VehicleMakeEntity>.makesToDomain(): List<VehicleMake> = map { it.toDomain() }
fun List<VehicleMake>.makesToEntity(): List<VehicleMakeEntity> = map { it.toEntity() }

fun List<VehicleModelEntity>.modelsToDomain(): List<VehicleModel> = map { it.toDomain() }
fun List<VehicleModel>.modelsToEntity(): List<VehicleModelEntity> = map { it.toEntity() }