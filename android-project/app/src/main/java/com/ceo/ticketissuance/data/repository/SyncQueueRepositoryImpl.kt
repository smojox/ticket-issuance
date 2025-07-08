package com.ceo.ticketissuance.data.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.data.database.dao.SyncQueueDao
import com.ceo.ticketissuance.domain.model.OperationType
import com.ceo.ticketissuance.domain.model.SyncQueueItem
import com.ceo.ticketissuance.domain.model.SyncStatus
import com.ceo.ticketissuance.domain.repository.SyncQueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncQueueRepositoryImpl @Inject constructor(
    private val syncQueueDao: SyncQueueDao
) : SyncQueueRepository {

    override fun getAllSyncQueue(): Flow<List<SyncQueueItem>> = flowOf(emptyList())
    override fun getSyncQueueByStatus(status: SyncStatus): Flow<List<SyncQueueItem>> = flowOf(emptyList())
    override fun getPendingSyncQueue(): Flow<List<SyncQueueItem>> = flowOf(emptyList())
    override fun getSyncQueueByOperation(operationType: OperationType): Flow<List<SyncQueueItem>> = flowOf(emptyList())
    override fun getPendingCount(): Flow<Int> = flowOf(0)
    override fun getFailedCount(): Flow<Int> = flowOf(0)
    override suspend fun getSyncQueueById(id: Long): Result<SyncQueueItem> = Result.Error(Exception("Not implemented"))
    override suspend fun insertSyncQueue(syncQueue: SyncQueueItem): Result<Long> = Result.Error(Exception("Not implemented"))
    override suspend fun insertSyncQueues(syncQueues: List<SyncQueueItem>): Result<Unit> = Result.Error(Exception("Not implemented"))
    override suspend fun updateSyncQueue(syncQueue: SyncQueueItem): Result<Unit> = Result.Error(Exception("Not implemented"))
    override suspend fun updateSyncQueueStatus(id: Long, status: SyncStatus): Result<Unit> = Result.Error(Exception("Not implemented"))
    override suspend fun incrementRetryCount(id: Long): Result<Unit> = Result.Error(Exception("Not implemented"))
    override suspend fun deleteSyncQueue(syncQueue: SyncQueueItem): Result<Unit> = Result.Error(Exception("Not implemented"))
    override suspend fun deleteCompletedSyncQueue(): Result<Unit> = Result.Error(Exception("Not implemented"))
}