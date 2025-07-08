package com.ceo.ticketissuance.data.mapper

import com.ceo.ticketissuance.data.database.entity.UserEntity
import com.ceo.ticketissuance.domain.model.User

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        username = username,
        fullName = fullName,
        badgeNumber = badgeNumber,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun User.toEntity(password: String = ""): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        password = password,
        fullName = fullName,
        badgeNumber = badgeNumber,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun List<UserEntity>.toDomain(): List<User> = map { it.toDomain() }
fun List<User>.toEntity(): List<UserEntity> = map { it.toEntity() }