package com.ceo.ticketissuance.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "sync_queue",
    indices = [Index(value = ["status"])]
)
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "operation_type")
    val operationType: String,
    
    @ColumnInfo(name = "table_name")
    val tableName: String,
    
    @ColumnInfo(name = "record_id")
    val recordId: Long?,
    
    @ColumnInfo(name = "data_json")
    val dataJson: String,
    
    @ColumnInfo(name = "status")
    val status: String,
    
    @ColumnInfo(name = "priority")
    val priority: String,
    
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    
    @ColumnInfo(name = "max_retries")
    val maxRetries: Int = 3,
    
    @ColumnInfo(name = "last_attempt_time")
    val lastAttemptTime: LocalDateTime?,
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime
)