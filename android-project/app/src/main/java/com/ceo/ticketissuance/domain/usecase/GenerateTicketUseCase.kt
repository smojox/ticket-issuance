package com.ceo.ticketissuance.domain.usecase

import com.ceo.ticketissuance.core.session.SessionManager
import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.Contravention
import com.ceo.ticketissuance.domain.model.Observation
import com.ceo.ticketissuance.domain.model.ObservationStatus
import com.ceo.ticketissuance.domain.model.Street
import com.ceo.ticketissuance.domain.model.Ticket
import com.ceo.ticketissuance.domain.model.TicketStatus
import com.ceo.ticketissuance.domain.model.VehicleMake
import com.ceo.ticketissuance.domain.model.VehicleModel
import com.ceo.ticketissuance.domain.repository.ContraventionRepository
import com.ceo.ticketissuance.domain.repository.ObservationRepository
import com.ceo.ticketissuance.domain.repository.StreetRepository
import com.ceo.ticketissuance.domain.repository.TicketRepository
import com.ceo.ticketissuance.domain.repository.VehicleRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GenerateTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val observationRepository: ObservationRepository,
    private val streetRepository: StreetRepository,
    private val contraventionRepository: ContraventionRepository,
    private val vehicleRepository: VehicleRepository,
    private val sessionManager: SessionManager
) {
    
    suspend operator fun invoke(
        observationId: Long,
        officerSignature: String? = null,
        additionalNotes: String = ""
    ): Result<TicketGenerationResult> {
        return try {
            // Get current user
            val currentUser = sessionManager.currentUser.value
                ?: return Result.Error(Exception("User not authenticated"))
            
            // Get observation details
            val observation = when (val result = observationRepository.getObservationById(observationId)) {
                is Result.Success -> result.data
                is Result.Error -> return Result.Error(Exception("Observation not found"))
                is Result.Loading -> return Result.Error(Exception("Loading observation data"))
            }
            
            // Validate observation status
            if (observation.status != ObservationStatus.COMPLETED) {
                return Result.Error(Exception("Observation is not ready for ticket issuance"))
            }
            
            // Get related data
            val street = when (val result = streetRepository.getStreetById(observation.streetId)) {
                is Result.Success -> result.data
                is Result.Error -> return Result.Error(Exception("Street not found"))
                is Result.Loading -> return Result.Error(Exception("Loading street data"))
            }
            
            val contravention = when (val result = contraventionRepository.getContraventionById(observation.contraventionId)) {
                is Result.Success -> result.data
                is Result.Error -> return Result.Error(Exception("Contravention not found"))
                is Result.Loading -> return Result.Error(Exception("Loading contravention data"))
            }
            
            val vehicleMake = when (val result = vehicleRepository.getVehicleMakeById(observation.makeId)) {
                is Result.Success -> result.data
                is Result.Error -> null
                is Result.Loading -> null
            }
            
            val vehicleModel = when (val result = vehicleRepository.getVehicleModelById(observation.modelId)) {
                is Result.Success -> result.data
                is Result.Error -> null
                is Result.Loading -> null
            }
            
            // Generate TMA 2004 compliant ticket number
            val ticketNumber = generateTmaTicketNumber()
            
            // Calculate penalty amounts based on contravention
            val penaltyAmount = contravention.penaltyAmount
            val discountAmount = contravention.discountAmount
            
            // Create ticket
            val ticket = Ticket(
                ticketNumber = ticketNumber,
                vrm = observation.vrm,
                streetId = observation.streetId,
                contraventionId = observation.contraventionId,
                makeId = vehicleMake?.id ?: 0,
                modelId = vehicleModel?.id ?: 0,
                color = "Unknown", // Vehicle color not stored in observation
                valvePositionFront = observation.valvePositionFront,
                valvePositionRear = observation.valvePositionRear,
                observationId = observationId,
                issueTime = LocalDateTime.now(),
                penaltyAmount = penaltyAmount,
                discountAmount = discountAmount,
                photoPath = observation.photoPath,
                notes = buildTicketNotes(observation, additionalNotes),
                status = TicketStatus.ISSUED,
                userId = currentUser.id,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            // Save ticket
            val ticketId = when (val result = ticketRepository.insertTicket(ticket)) {
                is Result.Success -> result.data
                is Result.Error -> return Result.Error(Exception("Failed to save ticket"))
                is Result.Loading -> return Result.Error(Exception("Saving ticket"))
            }
            
            // Update observation status
            val updatedObservation = observation.copy(
                status = ObservationStatus.COMPLETED,
                updatedAt = LocalDateTime.now()
            )
            
            when (val result = observationRepository.updateObservation(updatedObservation)) {
                is Result.Success -> {
                    // Success - return complete result
                    Result.Success(
                        TicketGenerationResult(
                            ticketId = ticketId,
                            ticket = ticket.copy(id = ticketId),
                            observation = updatedObservation,
                            street = street,
                            contravention = contravention,
                            vehicleMake = vehicleMake,
                            vehicleModel = vehicleModel,
                            officerName = "${currentUser.firstName} ${currentUser.lastName}",
                            badgeNumber = currentUser.badgeNumber,
                            officerSignature = officerSignature
                        )
                    )
                }
                is Result.Error -> Result.Error(Exception("Failed to update observation"))
                is Result.Loading -> Result.Error(Exception("Updating observation"))
            }
            
        } catch (e: Exception) {
            Result.Error(Exception(e.message ?: "Failed to generate ticket"))
        }
    }
    
    private suspend fun generateTmaTicketNumber(): String {
        val now = LocalDateTime.now()
        val dateCode = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        
        // Get today's ticket count to generate sequence
        val todayCount = when (val result = ticketRepository.getTodayTicketCount()) {
            is Result.Success -> result.data
            is Result.Error -> 0
        }
        
        val sequence = (todayCount + 1).toString().padStart(4, '0')
        
        // TMA 2004 compliant format: TMA-YYYYMMDD-NNNN
        return "TMA-$dateCode-$sequence"
    }
    
    private fun buildTicketNotes(observation: Observation, additionalNotes: String): String {
        val notes = StringBuilder()
        
        // Add observation details
        notes.append("Observation Details:\n")
        notes.append("- VRM: ${observation.vrm}\n")
        notes.append("- Start Time: ${observation.observationStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}\n")
        observation.observationEndTime?.let {
            notes.append("- End Time: ${it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}\n")
        }
        
        // Add additional notes
        if (additionalNotes.isNotBlank()) {
            notes.append("\nAdditional Notes:\n$additionalNotes")
        }
        
        return notes.toString()
    }
}

data class TicketGenerationResult(
    val ticketId: Long,
    val ticket: Ticket,
    val observation: Observation,
    val street: Street,
    val contravention: Contravention,
    val vehicleMake: VehicleMake?,
    val vehicleModel: VehicleModel?,
    val officerName: String,
    val badgeNumber: String,
    val officerSignature: String?
)