package com.ceo.ticketissuance.data.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.data.database.dao.TicketDao
import com.ceo.ticketissuance.domain.model.Ticket
import com.ceo.ticketissuance.domain.model.TicketStatus
import com.ceo.ticketissuance.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepositoryImpl @Inject constructor(
    private val ticketDao: TicketDao
) : TicketRepository {

    override fun getAllTickets(): Flow<List<Ticket>> = flowOf(emptyList())
    override fun getTicketsByStatus(status: TicketStatus): Flow<List<Ticket>> = flowOf(emptyList())
    override fun getTicketsByUser(userId: Long): Flow<List<Ticket>> = flowOf(emptyList())
    override fun getTicketsByVrm(vrm: String): Flow<List<Ticket>> = flowOf(emptyList())
    override suspend fun getTicketById(id: Long): Result<Ticket> = Result.Error(Exception("Not implemented"))
    override suspend fun getTicketByNumber(ticketNumber: String): Result<Ticket> = Result.Error(Exception("Not implemented"))
    override suspend fun getTicketByObservation(observationId: Long): Result<Ticket> = Result.Error(Exception("Not implemented"))
    override suspend fun getTicketCountByDate(date: String): Result<Int> = Result.Error(Exception("Not implemented"))
    override suspend fun getTodayTicketCount(): Result<Int> = Result.Error(Exception("Not implemented"))
    override suspend fun insertTicket(ticket: Ticket): Result<Long> = Result.Error(Exception("Not implemented"))
    override suspend fun insertTickets(tickets: List<Ticket>): Result<Unit> = Result.Error(Exception("Not implemented"))
    override suspend fun updateTicket(ticket: Ticket): Result<Unit> = Result.Error(Exception("Not implemented"))
    override suspend fun updateTicketStatus(id: Long, status: TicketStatus): Result<Unit> = Result.Error(Exception("Not implemented"))
    override suspend fun deleteTicket(ticket: Ticket): Result<Unit> = Result.Error(Exception("Not implemented"))
}