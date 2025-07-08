package com.ceo.ticketissuance.domain.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.Ticket
import com.ceo.ticketissuance.domain.model.TicketStatus
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    fun getAllTickets(): Flow<List<Ticket>>
    fun getTicketsByStatus(status: TicketStatus): Flow<List<Ticket>>
    fun getTicketsByUser(userId: Long): Flow<List<Ticket>>
    fun getTicketsByVrm(vrm: String): Flow<List<Ticket>>
    suspend fun getTicketById(id: Long): Result<Ticket>
    suspend fun getTicketByNumber(ticketNumber: String): Result<Ticket>
    suspend fun getTicketByObservation(observationId: Long): Result<Ticket>
    suspend fun getTicketCountByDate(date: String): Result<Int>
    suspend fun getTodayTicketCount(): Result<Int>
    suspend fun insertTicket(ticket: Ticket): Result<Long>
    suspend fun insertTickets(tickets: List<Ticket>): Result<Unit>
    suspend fun updateTicket(ticket: Ticket): Result<Unit>
    suspend fun updateTicketStatus(id: Long, status: TicketStatus): Result<Unit>
    suspend fun deleteTicket(ticket: Ticket): Result<Unit>
}