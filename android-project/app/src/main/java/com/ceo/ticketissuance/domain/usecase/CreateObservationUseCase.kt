package com.ceo.ticketissuance.domain.usecase

import com.ceo.ticketissuance.core.session.SessionManager
import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.Observation
import com.ceo.ticketissuance.domain.model.ObservationStatus
import com.ceo.ticketissuance.domain.repository.ObservationRepository
import com.ceo.ticketissuance.domain.repository.StreetRepository
import com.ceo.ticketissuance.domain.repository.VehicleRepository
import javax.inject.Inject

class CreateObservationUseCase @Inject constructor(
    private val observationRepository: ObservationRepository,
    private val streetRepository: StreetRepository,
    private val vehicleRepository: VehicleRepository,
    private val sessionManager: SessionManager
) {
    
    suspend operator fun invoke(
        plateNumber: String,
        streetId: Long,
        contraventionId: Long,
        makeId: Long,
        modelId: Long,
        photoFilename: String?
    ): Result<Long> {
        return try {
            val currentUser = sessionManager.currentUser.value
                ?: return Result.Error(Exception("No user logged in"))
            
            // Get street information
            val street = when (val streetResult = streetRepository.getStreetById(streetId)) {
                is Result.Success -> streetResult.data
                is Result.Error -> return Result.Error(Exception("Street not found"))
                is Result.Loading -> return Result.Error(Exception("Loading street data"))
            }
            
            val observation = Observation(
                id = 0, // Will be assigned by database
                vrm = plateNumber,
                streetId = streetId,
                contraventionId = contraventionId,
                makeId = makeId,
                modelId = modelId,
                valvePositionFront = null,
                valvePositionRear = null,
                observationStartTime = java.time.LocalDateTime.now(),
                observationEndTime = null,
                status = ObservationStatus.ACTIVE,
                photoPath = photoFilename,
                userId = currentUser.id,
                createdAt = java.time.LocalDateTime.now(),
                updatedAt = java.time.LocalDateTime.now()
            )
            
            observationRepository.insertObservation(observation)
        } catch (e: Exception) {
            Result.Error(Exception(e.message ?: "Failed to create observation"))
        }
    }
}

data class ObservationFormData(
    val plateNumber: String,
    val streetId: Long,
    val photoFilename: String?,
    val notes: String = "",
    val location: String = "",
    val vehicleMake: String? = null,
    val vehicleModel: String? = null,
    val vehicleColour: String? = null
)