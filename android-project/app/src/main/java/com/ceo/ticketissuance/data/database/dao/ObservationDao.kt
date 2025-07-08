package com.ceo.ticketissuance.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ceo.ticketissuance.data.database.entity.ObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {
    
    @Query("SELECT * FROM observations ORDER BY created_at DESC")
    fun getAllObservations(): Flow<List<ObservationEntity>>
    
    @Query("SELECT * FROM observations WHERE status = :status ORDER BY created_at DESC")
    fun getObservationsByStatus(status: String): Flow<List<ObservationEntity>>
    
    @Query("SELECT * FROM observations WHERE user_id = :userId ORDER BY created_at DESC")
    fun getObservationsByUser(userId: Long): Flow<List<ObservationEntity>>
    
    @Query("SELECT * FROM observations WHERE id = :id")
    suspend fun getObservationById(id: Long): ObservationEntity?
    
    @Query("SELECT * FROM observations WHERE vrm = :vrm ORDER BY created_at DESC")
    fun getObservationsByVrm(vrm: String): Flow<List<ObservationEntity>>
    
    @Query("SELECT * FROM observations WHERE status = 'active' ORDER BY observation_start_time ASC")
    fun getActiveObservations(): Flow<List<ObservationEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: ObservationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservations(observations: List<ObservationEntity>)
    
    @Update
    suspend fun updateObservation(observation: ObservationEntity)
    
    @Delete
    suspend fun deleteObservation(observation: ObservationEntity)
    
    @Query("DELETE FROM observations")
    suspend fun deleteAllObservations()
    
    @Query("UPDATE observations SET status = :status WHERE id = :id")
    suspend fun updateObservationStatus(id: Long, status: String)
}