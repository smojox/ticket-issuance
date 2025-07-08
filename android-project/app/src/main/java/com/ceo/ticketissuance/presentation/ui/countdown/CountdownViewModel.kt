package com.ceo.ticketissuance.presentation.ui.countdown

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ceo.ticketissuance.core.service.ObservationTimerService
import com.ceo.ticketissuance.core.service.pauseObservationTimer
import com.ceo.ticketissuance.core.service.resumeObservationTimer
import com.ceo.ticketissuance.core.service.stopObservationTimer
import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.Observation
import com.ceo.ticketissuance.domain.model.ObservationTimer
import com.ceo.ticketissuance.domain.repository.ObservationRepository
import com.ceo.ticketissuance.domain.usecase.CompleteObservationUseCase
import com.ceo.ticketissuance.domain.usecase.ObservationOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountdownViewModel @Inject constructor(
    private val observationRepository: ObservationRepository,
    private val completeObservationUseCase: CompleteObservationUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CountdownUiState())
    val uiState: StateFlow<CountdownUiState> = _uiState.asStateFlow()
    
    private var currentObservationId: Long = -1L
    
    init {
        // Observe timer service for real-time updates
        observeTimerService()
        // Observe active timers from repository
        observeActiveTimers()
    }
    
    fun loadObservation(observationId: Long) {
        currentObservationId = observationId
        viewModelScope.launch {
            try {
                // Load observation details
                when (val observationResult = observationRepository.getObservationById(observationId)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            observation = observationResult.data,
                            isLoading = false
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = observationResult.message,
                            isLoading = false
                        )
                    }
                }
                
                // Load timer state
                loadTimerState(observationId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to load observation",
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadTimerState(observationId: Long) {
        viewModelScope.launch {
            when (val timerResult = observationRepository.getObservationTimer(observationId)) {
                is Result.Success -> {
                    val timer = timerResult.data
                    _uiState.value = _uiState.value.copy(timer = timer)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = timerResult.message
                    )
                }
            }
        }
    }
    
    private fun observeTimerService() {
        viewModelScope.launch {
            ObservationTimerService.instance?.timerStates?.collect { timerStates ->
                val currentTimer = timerStates[currentObservationId]
                if (currentTimer != null) {
                    _uiState.value = _uiState.value.copy(timer = currentTimer)
                }
            }
        }
    }
    
    private fun observeActiveTimers() {
        viewModelScope.launch {
            observationRepository.getActiveTimers().collect { activeTimers ->
                _uiState.value = _uiState.value.copy(activeTimers = activeTimers)
            }
        }
    }
    
    fun pauseTimer() {
        if (currentObservationId != -1L) {
            context.pauseObservationTimer(currentObservationId)
        }
    }
    
    fun resumeTimer() {
        if (currentObservationId != -1L) {
            context.resumeObservationTimer(currentObservationId)
        }
    }
    
    fun stopTimer() {
        if (currentObservationId != -1L) {
            context.stopObservationTimer(currentObservationId)
        }
    }
    
    fun completeObservation(outcome: ObservationOutcome) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCompleting = true)
                
                when (val result = completeObservationUseCase(
                    observationId = currentObservationId,
                    outcome = outcome,
                    finalNotes = when (outcome) {
                        ObservationOutcome.VEHICLE_MOVED -> "Vehicle moved before timer completion"
                        ObservationOutcome.PROCEED_TO_TICKET -> "Proceeding to ticket issuance"
                        ObservationOutcome.CANCELLED_BY_OFFICER -> "Observation cancelled by officer"
                        ObservationOutcome.TIMER_EXPIRED -> "Timer expired - ready for ticket"
                    }
                )) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isCompleting = false,
                            isCompleted = true
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isCompleting = false,
                            errorMessage = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCompleting = false,
                    errorMessage = e.message ?: "Failed to complete observation"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun refreshTimerState() {
        if (currentObservationId != -1L) {
            loadTimerState(currentObservationId)
        }
    }
    
    // Handle timer completion from service
    fun onTimerCompleted() {
        viewModelScope.launch {
            completeObservation(ObservationOutcome.TIMER_EXPIRED)
        }
    }
    
    // Get timer statistics
    fun getTimerStats(): TimerStats? {
        val timer = _uiState.value.timer ?: return null
        
        return TimerStats(
            totalDurationMinutes = timer.durationMinutes,
            elapsedMinutes = timer.elapsedSeconds / 60,
            remainingMinutes = timer.remainingSeconds / 60,
            progressPercentage = timer.progressPercentage,
            isOnTime = timer.remainingSeconds > 0,
            isPaused = timer.isPaused,
            isCompleted = timer.isCompleted
        )
    }
}

data class CountdownUiState(
    val observation: Observation? = null,
    val timer: ObservationTimer? = null,
    val activeTimers: List<ObservationTimer> = emptyList(),
    val isLoading: Boolean = true,
    val isCompleting: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)

data class TimerStats(
    val totalDurationMinutes: Int,
    val elapsedMinutes: Int,
    val remainingMinutes: Int,
    val progressPercentage: Float,
    val isOnTime: Boolean,
    val isPaused: Boolean,
    val isCompleted: Boolean
) {
    val efficiency: String get() = when {
        isCompleted && isOnTime -> "Completed on time"
        isCompleted && !isOnTime -> "Overtime"
        progressPercentage > 0.8f -> "Nearly complete"
        progressPercentage > 0.5f -> "Halfway"
        progressPercentage > 0.2f -> "In progress"
        else -> "Just started"
    }
}