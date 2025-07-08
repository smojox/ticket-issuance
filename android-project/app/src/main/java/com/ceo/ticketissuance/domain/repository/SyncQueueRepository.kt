package com.ceo.ticketissuance.domain.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.OperationType
import com.ceo.ticketissuance.domain.model.SyncQueueItem
import com.ceo.ticketissuance.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface SyncQueueRepository {
    fun getAllSyncQueue(): Flow<List<SyncQueueItem>>
    fun getSyncQueueByStatus(status: SyncStatus): Flow<List<SyncQueueItem>>
    fun getPendingSyncQueue(): Flow<List<SyncQueueItem>>
    fun getSyncQueueByOperation(operationType: OperationType): Flow<List<SyncQueueItem>>
    fun getPendingCount(): Flow<Int>
    fun getFailedCount(): Flow<Int>
    suspend fun getSyncQueueById(id: Long): Result<SyncQueueItem>
    suspend fun insertSyncQueue(syncQueue: SyncQueueItem): Result<Long>
    suspend fun insertSyncQueues(syncQueues: List<SyncQueueItem>): Result<Unit>
    suspend fun updateSyncQueue(syncQueue: SyncQueueItem): Result<Unit>
    suspend fun updateSyncQueueStatus(id: Long, status: SyncStatus): Result<Unit>
    suspend fun incrementRetryCount(id: Long): Result<Unit>
    suspend fun deleteSyncQueue(syncQueue: SyncQueueItem): Result<Unit>
    suspend fun deleteCompletedSyncQueue(): Result<Unit>
}