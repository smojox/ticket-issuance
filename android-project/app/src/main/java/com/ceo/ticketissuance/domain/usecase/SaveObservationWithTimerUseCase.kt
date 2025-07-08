package com.ceo.ticketissuance.domain.usecase

import android.content.Context
import com.ceo.ticketissuance.core.service.startObservationTimer
import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.Observation
import com.ceo.ticketissuance.domain.model.ObservationStatus
import com.ceo.ticketissuance.domain.repository.ObservationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SaveObservationWithTimerUseCase @Inject constructor(
    private val createObservationUseCase: CreateObservationUseCase,
    private val startObservationTimerUseCase: StartObservationTimerUseCase,
    @ApplicationContext private val context: Context
) {
    
    suspend operator fun invoke(
        plateNumber: String,
        streetId: Long,
        contraventionId: Long,
        observationPeriodMinutes: Int,
        photoFilename: String?,
        notes: String = "",
        location: String = "",
        vehicleMake: String? = null,
        vehicleModel: String? = null,
        vehicleColour: String? = null
    ): Result<ObservationSaveResult> {
        return try {
            // Create the observation
            when (val observationResult = createObservationUseCase(
                plateNumber = plateNumber,
                streetId = streetId,
                photoFilename = photoFilename,
                notes = notes,
                location = location
            )) {
                is Result.Success -> {
                    val observationId = observationResult.data
                    
                    // Start the observation timer using the service
                    context.startObservationTimer(
                        observationId = observationId,
                        durationMinutes = observationPeriodMinutes,
                        plateNumber = plateNumber
                    )
                    
                    // Start the timer in repository for tracking
                    when (val timerResult = startObservationTimerUseCase(
                        observationId = observationId,
                        plateNumber = plateNumber,
                        durationMinutes = observationPeriodMinutes
                    )) {
                        is Result.Success -> {
                            Result.Success(
                                ObservationSaveResult(
                                    observationId = observationId,
                                    timerStarted = true,
                                    observationPeriodMinutes = observationPeriodMinutes
                                )
                            )
                        }
                        is Result.Error -> {
                            // Observation created but timer failed to start
                            Result.Success(
                                ObservationSaveResult(
                                    observationId = observationId,
                                    timerStarted = false,
                                    observationPeriodMinutes = observationPeriodMinutes,
                                    timerError = timerResult.message
                                )
                            )
                        }
                    }
                }
                is Result.Error -> Result.Error(observationResult.message)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save observation")
        }
    }
}

data class ObservationSaveResult(
    val observationId: Long,
    val timerStarted: Boolean,
    val observationPeriodMinutes: Int,
    val timerError: String? = null
)

class UpdateObservationUseCase @Inject constructor(
    private val observationRepository: ObservationRepository
) {
    
    suspend operator fun invoke(
        observationId: Long,
        plateNumber: String? = null,
        vehicleMake: String? = null,
        vehicleModel: String? = null,
        vehicleColour: String? = null,
        location: String? = null,
        notes: String? = null,
        status: ObservationStatus? = null
    ): Result<Unit> {
        return try {
            // Get current observation
            when (val currentResult = observationRepository.getObservationById(observationId)) {
                is Result.Success -> {
                    val current = currentResult.data
                    
                    // Create updated observation
                    val updated = current.copy(
                        vrm = plateNumber ?: current.vrm,
                        vehicleMake = vehicleMake ?: current.vehicleMake,
                        vehicleModel = vehicleModel ?: current.vehicleModel,
                        vehicleColour = vehicleColour ?: current.vehicleColour,
                        location = location ?: current.location,
                        notes = notes ?: current.notes,
                        status = status ?: current.status
                    )
                    
                    observationRepository.updateObservation(updated)
                }
                is Result.Error -> Result.Error(currentResult.message)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update observation")
        }
    }
}

class CompleteObservationUseCase @Inject constructor(
    private val observationRepository: ObservationRepository,
    private val stopObservationTimerUseCase: StopObservationTimerUseCase
) {
    
    suspend operator fun invoke(
        observationId: Long,
        outcome: ObservationOutcome,
        finalNotes: String = ""
    ): Result<Unit> {
        return try {
            // Stop the timer
            stopObservationTimerUseCase(observationId)
            
            // Update observation status
            val newStatus = when (outcome) {
                ObservationOutcome.VEHICLE_MOVED -> ObservationStatus.COMPLETED
                ObservationOutcome.PROCEED_TO_TICKET -> ObservationStatus.PENDING_TICKET
                ObservationOutcome.CANCELLED_BY_OFFICER -> ObservationStatus.CANCELLED
                ObservationOutcome.TIMER_EXPIRED -> ObservationStatus.READY_FOR_TICKET
            }
            
            when (val observationResult = observationRepository.getObservationById(observationId)) {
                is Result.Success -> {
                    val updated = observationResult.data.copy(
                        status = newStatus,
                        notes = if (finalNotes.isNotBlank()) {
                            "${observationResult.data.notes}\n\nFinal: $finalNotes"
                        } else {
                            observationResult.data.notes
                        },
                        countdownEndTime = System.currentTimeMillis()
                    )
                    
                    observationRepository.updateObservation(updated)
                }
                is Result.Error -> Result.Error(observationResult.message)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to complete observation")
        }
    }
}

enum class ObservationOutcome {
    VEHICLE_MOVED,
    PROCEED_TO_TICKET,
    CANCELLED_BY_OFFICER,
    TIMER_EXPIRED
}