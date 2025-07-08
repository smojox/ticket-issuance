package com.ceo.ticketissuance.presentation.ui.countdown

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ceo.ticketissuance.core.service.pauseObservationTimer
import com.ceo.ticketissuance.core.service.resumeObservationTimer
import com.ceo.ticketissuance.core.service.stopObservationTimer
import com.ceo.ticketissuance.domain.model.ObservationTimer
import com.ceo.ticketissuance.domain.repository.ObservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerManagementViewModel @Inject constructor(
    private val observationRepository: ObservationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TimerManagementUiState())
    val uiState: StateFlow<TimerManagementUiState> = _uiState.asStateFlow()
    
    init {
        loadActiveTimers()
    }
    
    private fun loadActiveTimers() {
        viewModelScope.launch {
            observationRepository.getActiveTimers().collect { timers ->
                _uiState.value = _uiState.value.copy(
                    activeTimers = timers.sortedBy { it.remainingSeconds },
                    isLoading = false
                )
            }
        }
    }
    
    fun pauseTimer(observationId: Long) {
        context.pauseObservationTimer(observationId)
    }
    
    fun resumeTimer(observationId: Long) {
        context.resumeObservationTimer(observationId)
    }
    
    fun stopTimer(observationId: Long) {
        context.stopObservationTimer(observationId)
    }
    
    fun refreshTimers() {
        loadActiveTimers()
    }
    
    fun getTimerStats(): TimerManagementStats {
        val timers = _uiState.value.activeTimers
        
        return TimerManagementStats(
            totalActive = timers.count { it.isActive },
            totalPaused = timers.count { it.isPaused },
            totalCompleted = timers.count { it.isCompleted },
            urgentCount = timers.count { it.remainingSeconds <= 300 },
            criticalCount = timers.count { it.remainingSeconds <= 60 },
            averageProgress = if (timers.isNotEmpty()) {
                timers.map { it.progressPercentage }.average().toFloat()
            } else 0f
        )
    }
}

data class TimerManagementUiState(
    val activeTimers: List<ObservationTimer> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class TimerManagementStats(
    val totalActive: Int,
    val totalPaused: Int,
    val totalCompleted: Int,
    val urgentCount: Int,
    val criticalCount: Int,
    val averageProgress: Float
) {
    val hasUrgentTimers: Boolean get() = urgentCount > 0
    val hasCriticalTimers: Boolean get() = criticalCount > 0
    val overallStatus: String get() = when {
        hasCriticalTimers -> "Critical"
        hasUrgentTimers -> "Urgent"
        totalActive > 0 -> "Active"
        else -> "Idle"
    }
}