package com.ceo.ticketissuance.data.mapper

import com.ceo.ticketissuance.data.database.entity.SyncQueueEntity
import com.ceo.ticketissuance.domain.model.OperationType
import com.ceo.ticketissuance.domain.model.Priority
import com.ceo.ticketissuance.domain.model.SyncQueueItem
import com.ceo.ticketissuance.domain.model.SyncStatus

fun SyncQueueEntity.toDomain(): SyncQueueItem {
    return SyncQueueItem(
        id = id,
        operationType = OperationType.valueOf(operationType.uppercase()),
        tableName = tableName,
        recordId = recordId,
        dataJson = dataJson,
        status = SyncStatus.valueOf(status.uppercase()),
        priority = Priority.valueOf(priority.uppercase()),
        retryCount = retryCount,
        maxRetries = maxRetries,
        lastAttemptTime = lastAttemptTime,
        errorMessage = errorMessage,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun SyncQueueItem.toEntity(): SyncQueueEntity {
    return SyncQueueEntity(
        id = id,
        operationType = operationType.name.lowercase(),
        tableName = tableName,
        recordId = recordId,
        dataJson = dataJson,
        status = status.name.lowercase(),
        priority = priority.name.lowercase(),
        retryCount = retryCount,
        maxRetries = maxRetries,
        lastAttemptTime = lastAttemptTime,
        errorMessage = errorMessage,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun List<SyncQueueEntity>.toDomain(): List<SyncQueueItem> = map { it.toDomain() }
fun List<SyncQueueItem>.toEntity(): List<SyncQueueEntity> = map { it.toEntity() }