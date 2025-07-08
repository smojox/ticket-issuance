package com.ceo.ticketissuance.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ceo.ticketissuance.data.database.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    
    @Query("SELECT * FROM sync_queue ORDER BY created_at DESC")
    fun getAllSyncQueue(): Flow<List<SyncQueueEntity>>
    
    @Query("SELECT * FROM sync_queue WHERE status = :status ORDER BY priority ASC, created_at ASC")
    fun getSyncQueueByStatus(status: String): Flow<List<SyncQueueEntity>>
    
    @Query("SELECT * FROM sync_queue WHERE status = 'pending' ORDER BY priority ASC, created_at ASC")
    fun getPendingSyncQueue(): Flow<List<SyncQueueEntity>>
    
    @Query("SELECT * FROM sync_queue WHERE operation_type = :operationType ORDER BY created_at DESC")
    fun getSyncQueueByOperation(operationType: String): Flow<List<SyncQueueEntity>>
    
    @Query("SELECT * FROM sync_queue WHERE id = :id")
    suspend fun getSyncQueueById(id: Long): SyncQueueEntity?
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'pending'")
    fun getPendingCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'failed'")
    fun getFailedCount(): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncQueue(syncQueue: SyncQueueEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncQueues(syncQueues: List<SyncQueueEntity>)
    
    @Update
    suspend fun updateSyncQueue(syncQueue: SyncQueueEntity)
    
    @Delete
    suspend fun deleteSyncQueue(syncQueue: SyncQueueEntity)
    
    @Query("DELETE FROM sync_queue WHERE status = 'completed'")
    suspend fun deleteCompletedSyncQueue()
    
    @Query("DELETE FROM sync_queue")
    suspend fun deleteAllSyncQueue()
    
    @Query("UPDATE sync_queue SET status = :status WHERE id = :id")
    suspend fun updateSyncQueueStatus(id: Long, status: String)
    
    @Query("UPDATE sync_queue SET retry_count = retry_count + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: Long)
}