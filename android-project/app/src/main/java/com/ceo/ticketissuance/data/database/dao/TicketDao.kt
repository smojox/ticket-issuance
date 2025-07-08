package com.ceo.ticketissuance.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ceo.ticketissuance.data.database.entity.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    
    @Query("SELECT * FROM tickets ORDER BY created_at DESC")
    fun getAllTickets(): Flow<List<TicketEntity>>
    
    @Query("SELECT * FROM tickets WHERE status = :status ORDER BY created_at DESC")
    fun getTicketsByStatus(status: String): Flow<List<TicketEntity>>
    
    @Query("SELECT * FROM tickets WHERE user_id = :userId ORDER BY created_at DESC")
    fun getTicketsByUser(userId: Long): Flow<List<TicketEntity>>
    
    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getTicketById(id: Long): TicketEntity?
    
    @Query("SELECT * FROM tickets WHERE ticket_number = :ticketNumber")
    suspend fun getTicketByNumber(ticketNumber: String): TicketEntity?
    
    @Query("SELECT * FROM tickets WHERE vrm = :vrm ORDER BY created_at DESC")
    fun getTicketsByVrm(vrm: String): Flow<List<TicketEntity>>
    
    @Query("SELECT * FROM tickets WHERE observation_id = :observationId")
    suspend fun getTicketByObservation(observationId: Long): TicketEntity?
    
    @Query("SELECT COUNT(*) FROM tickets WHERE DATE(created_at) = DATE(:date)")
    suspend fun getTicketCountByDate(date: String): Int
    
    @Query("SELECT COUNT(*) FROM tickets WHERE DATE(created_at) = DATE('now')")
    suspend fun getTodayTicketCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)
    
    @Update
    suspend fun updateTicket(ticket: TicketEntity)
    
    @Delete
    suspend fun deleteTicket(ticket: TicketEntity)
    
    @Query("DELETE FROM tickets")
    suspend fun deleteAllTickets()
    
    @Query("UPDATE tickets SET status = :status WHERE id = :id")
    suspend fun updateTicketStatus(id: Long, status: String)
}