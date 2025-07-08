package com.ceo.ticketissuance.presentation.ui.ticketissuance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ceo.ticketissuance.core.printing.TicketPrintingService
import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.Contravention
import com.ceo.ticketissuance.domain.model.Observation
import com.ceo.ticketissuance.domain.model.Street
import com.ceo.ticketissuance.domain.model.VehicleMake
import com.ceo.ticketissuance.domain.model.VehicleModel
import com.ceo.ticketissuance.domain.repository.ContraventionRepository
import com.ceo.ticketissuance.domain.repository.ObservationRepository
import com.ceo.ticketissuance.domain.repository.StreetRepository
import com.ceo.ticketissuance.domain.repository.VehicleRepository
import com.ceo.ticketissuance.domain.usecase.GenerateTicketUseCase
import com.ceo.ticketissuance.domain.usecase.TicketGenerationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketIssuanceViewModel @Inject constructor(
    private val observationRepository: ObservationRepository,
    private val streetRepository: StreetRepository,
    private val contraventionRepository: ContraventionRepository,
    private val vehicleRepository: VehicleRepository,
    private val generateTicketUseCase: GenerateTicketUseCase,
    private val ticketPrintingService: TicketPrintingService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TicketIssuanceUiState())
    val uiState: StateFlow<TicketIssuanceUiState> = _uiState.asStateFlow()
    
    fun loadObservation(observationId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Load observation
                val observation = when (val result = observationRepository.getObservationById(observationId)) {
                    is Result.Success -> result.data
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        return@launch
                    }
                }
                
                // Load street
                val street = when (val result = streetRepository.getStreetById(observation.streetId)) {
                    is Result.Success -> result.data
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to load street: ${result.message}"
                        )
                        return@launch
                    }
                }
                
                // Load contravention
                val contravention = when (val result = contraventionRepository.getContraventionById(observation.contraventionId)) {
                    is Result.Success -> result.data
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to load contravention: ${result.message}"
                        )
                        return@launch
                    }
                }
                
                // Load vehicle make and model if available
                val vehicleMake = observation.vehicleMakeId?.let { makeId ->
                    when (val result = vehicleRepository.getVehicleMakeById(makeId)) {
                        is Result.Success -> result.data
                        is Result.Error -> null
                    }
                }
                
                val vehicleModel = observation.vehicleModelId?.let { modelId ->
                    when (val result = vehicleRepository.getVehicleModelById(modelId)) {
                        is Result.Success -> result.data
                        is Result.Error -> null
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoaded = true,
                    observation = observation,
                    street = street,
                    contravention = contravention,
                    vehicleMake = vehicleMake,
                    vehicleModel = vehicleModel
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load observation data"
                )
            }
        }
    }
    
    fun generateTicket(
        observationId: Long,
        additionalNotes: String = "",
        officerSignature: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                when (val result = generateTicketUseCase(
                    observationId = observationId,
                    officerSignature = officerSignature,
                    additionalNotes = additionalNotes
                )) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            ticket = result.data
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to generate ticket"
                )
            }
        }
    }
    
    fun setOfficerSignature(signature: String) {
        _uiState.value = _uiState.value.copy(officerSignature = signature)
    }
    
    fun printTicket(ticketResult: TicketGenerationResult) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPrinting = true, errorMessage = null)
            
            when (val result = ticketPrintingService.printTicket(
                ticketResult = ticketResult,
                onPrintStarted = {
                    _uiState.value = _uiState.value.copy(isPrinting = true)
                },
                onPrintCompleted = {
                    _uiState.value = _uiState.value.copy(isPrinting = false, printSuccess = true)
                },
                onPrintFailed = { error ->
                    _uiState.value = _uiState.value.copy(isPrinting = false, errorMessage = error)
                }
            )) {
                is Result.Success -> {
                    // Print dialog will be shown by the system
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isPrinting = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearPrintSuccess() {
        _uiState.value = _uiState.value.copy(printSuccess = false)
    }
}

data class TicketIssuanceUiState(
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val observation: Observation? = null,
    val street: Street? = null,
    val contravention: Contravention? = null,
    val vehicleMake: VehicleMake? = null,
    val vehicleModel: VehicleModel? = null,
    val ticket: TicketGenerationResult? = null,
    val officerSignature: String? = null,
    val isPrinting: Boolean = false,
    val printSuccess: Boolean = false,
    val errorMessage: String? = null
)