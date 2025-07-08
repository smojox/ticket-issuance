package com.ceo.ticketissuance.domain.model

import java.time.LocalDateTime

data class SyncQueueItem(
    val id: Long = 0,
    val operationType: OperationType,
    val tableName: String,
    val recordId: Long?,
    val dataJson: String,
    val status: SyncStatus,
    val priority: Priority,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val lastAttemptTime: LocalDateTime?,
    val errorMessage: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class OperationType {
    UPLOAD_TICKET,
    UPLOAD_OBSERVATION,
    UPLOAD_PHOTO,
    DOWNLOAD_STREETS,
    DOWNLOAD_CONTRAVENTIONS,
    DOWNLOAD_VEHICLE_DATA,
    DOWNLOAD_UPDATES
}

enum class SyncStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}