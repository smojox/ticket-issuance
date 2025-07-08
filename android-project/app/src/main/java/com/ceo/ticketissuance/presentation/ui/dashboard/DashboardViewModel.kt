package com.ceo.ticketissuance.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ceo.ticketissuance.core.session.SessionManager
import com.ceo.ticketissuance.domain.model.ObservationStatus
import com.ceo.ticketissuance.domain.model.SyncStatus
import com.ceo.ticketissuance.domain.repository.ObservationRepository
import com.ceo.ticketissuance.domain.repository.SyncQueueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val observationRepository: ObservationRepository,
    private val syncQueueRepository: SyncQueueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    init {
        loadDashboardData()
        startPeriodicUpdates()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Get current user info
            val currentUser = sessionManager.currentUser.value
            val currentTime = LocalDateTime.now()
            
            // Combine all flows for real-time updates
            combine(
                observationRepository.getObservationsByStatus(ObservationStatus.ACTIVE),
                syncQueueRepository.getPendingCount(),
                syncQueueRepository.getFailedCount()
            ) { activeObservations, pendingCount, failedCount ->
                _uiState.value = _uiState.value.copy(
                    currentUser = currentUser,
                    currentTime = currentTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    countdownCount = activeObservations.size,
                    queueCount = pendingCount,
                    hasFailedSync = failedCount > 0,
                    syncStatus = if (failedCount > 0) SyncStatus.FAILED else SyncStatus.COMPLETED
                )
            }
        }
    }

    private fun startPeriodicUpdates() {
        viewModelScope.launch {
            // Update time every minute
            while (true) {
                kotlinx.coroutines.delay(60000) // 1 minute
                val currentTime = LocalDateTime.now()
                _uiState.value = _uiState.value.copy(
                    currentTime = currentTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                )
            }
        }
    }

    fun onObservationClick() {
        viewModelScope.launch {
            _events.emit(DashboardEvent.NavigateToObservation)
        }
    }

    fun onIssuanceClick() {
        viewModelScope.launch {
            _events.emit(DashboardEvent.NavigateToIssuance)
        }
    }

    fun onCountdownClick() {
        viewModelScope.launch {
            _events.emit(DashboardEvent.NavigateToCountdown)
        }
    }

    fun onQueueClick() {
        viewModelScope.launch {
            _events.emit(DashboardEvent.NavigateToQueue)
        }
    }

    fun onSyncClick() {
        viewModelScope.launch {
            _events.emit(DashboardEvent.NavigateToSync)
        }
    }

    fun onTicketHistoryClick() {
        viewModelScope.launch {
            _events.emit(DashboardEvent.NavigateToTicketHistory)
        }
    }

    fun onLogoutClick() {
        sessionManager.logout()
        viewModelScope.launch {
            _events.emit(DashboardEvent.NavigateToLogin)
        }
    }

    fun refreshData() {
        loadDashboardData()
    }
}

data class DashboardUiState(
    val currentUser: com.ceo.ticketissuance.domain.model.User? = null,
    val currentTime: String = "",
    val countdownCount: Int = 0,
    val queueCount: Int = 0,
    val hasFailedSync: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.COMPLETED,
    val isConnected: Boolean = true // For future network status implementation
)

sealed class DashboardEvent {
    object NavigateToObservation : DashboardEvent()
    object NavigateToIssuance : DashboardEvent()
    object NavigateToCountdown : DashboardEvent()
    object NavigateToQueue : DashboardEvent()
    object NavigateToSync : DashboardEvent()
    object NavigateToTicketHistory : DashboardEvent()
    object NavigateToLogin : DashboardEvent()
}