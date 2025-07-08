package com.ceo.ticketissuance.presentation.ui.tickethistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ceo.ticketissuance.core.session.SessionManager
import com.ceo.ticketissuance.domain.model.Ticket
import com.ceo.ticketissuance.domain.repository.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class TicketHistoryViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TicketHistoryUiState())
    val uiState: StateFlow<TicketHistoryUiState> = _uiState.asStateFlow()
    
    fun loadTickets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Get current user
                val currentUser = sessionManager.getCurrentUser()
                
                if (currentUser != null) {
                    // Load tickets for current user
                    ticketRepository.getTicketsByUser(currentUser.id).collectLatest { tickets ->
                        val today = LocalDate.now()
                        val weekStart = today.minus(7, ChronoUnit.DAYS)
                        
                        val todayCount = tickets.count { 
                            it.issueTime.toLocalDate() == today 
                        }
                        
                        val weekCount = tickets.count { 
                            it.issueTime.toLocalDate().isAfter(weekStart) || 
                            it.issueTime.toLocalDate().isEqual(weekStart)
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            tickets = tickets,
                            filteredTickets = tickets,
                            todayCount = todayCount,
                            weekCount = weekCount
                        )
                    }
                } else {
                    // Load all tickets if no user (shouldn't happen in practice)
                    ticketRepository.getAllTickets().collectLatest { tickets ->
                        val today = LocalDate.now()
                        val weekStart = today.minus(7, ChronoUnit.DAYS)
                        
                        val todayCount = tickets.count { 
                            it.issueTime.toLocalDate() == today 
                        }
                        
                        val weekCount = tickets.count { 
                            it.issueTime.toLocalDate().isAfter(weekStart) || 
                            it.issueTime.toLocalDate().isEqual(weekStart)
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            tickets = tickets,
                            filteredTickets = tickets,
                            todayCount = todayCount,
                            weekCount = weekCount
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load tickets"
                )
            }
        }
    }
    
    fun searchTickets(query: String) {
        val tickets = _uiState.value.tickets
        val filteredTickets = if (query.isBlank()) {
            tickets
        } else {
            tickets.filter { ticket ->
                ticket.ticketNumber.contains(query, ignoreCase = true) ||
                ticket.vrm.contains(query, ignoreCase = true) ||
                ticket.notes?.contains(query, ignoreCase = true) == true
            }
        }
        
        _uiState.value = _uiState.value.copy(
            filteredTickets = filteredTickets,
            searchQuery = query
        )
    }
    
    fun filterByStatus(status: com.ceo.ticketissuance.domain.model.TicketStatus?) {
        val tickets = _uiState.value.tickets
        val filteredTickets = if (status == null) {
            tickets
        } else {
            tickets.filter { it.status == status }
        }
        
        _uiState.value = _uiState.value.copy(
            filteredTickets = filteredTickets,
            selectedStatus = status
        )
    }
    
    fun filterByDateRange(startDate: LocalDateTime?, endDate: LocalDateTime?) {
        val tickets = _uiState.value.tickets
        val filteredTickets = tickets.filter { ticket ->
            val issueDate = ticket.issueTime
            val afterStart = startDate?.let { issueDate.isAfter(it) || issueDate.isEqual(it) } ?: true
            val beforeEnd = endDate?.let { issueDate.isBefore(it) || issueDate.isEqual(it) } ?: true
            afterStart && beforeEnd
        }
        
        _uiState.value = _uiState.value.copy(
            filteredTickets = filteredTickets,
            dateRangeStart = startDate,
            dateRangeEnd = endDate
        )
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            filteredTickets = _uiState.value.tickets,
            searchQuery = "",
            selectedStatus = null,
            dateRangeStart = null,
            dateRangeEnd = null
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class TicketHistoryUiState(
    val isLoading: Boolean = false,
    val tickets: List<Ticket> = emptyList(),
    val filteredTickets: List<Ticket> = emptyList(),
    val todayCount: Int = 0,
    val weekCount: Int = 0,
    val searchQuery: String = "",
    val selectedStatus: com.ceo.ticketissuance.domain.model.TicketStatus? = null,
    val dateRangeStart: LocalDateTime? = null,
    val dateRangeEnd: LocalDateTime? = null,
    val errorMessage: String? = null
)