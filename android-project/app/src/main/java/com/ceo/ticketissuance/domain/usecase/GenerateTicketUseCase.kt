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
            val currentUser = sessionManager.getCurrentUser()
                ?: return Result.Error("User not authenticated")
            
            // Get observation details
            val observation = when (val result = observationRepository.getObservationById(observationId)) {
                is Result.Success -> result.data
                is Result.Error -> return Result.Error(result.message)
            }
            
            // Validate observation status
            if (observation.status != ObservationStatus.READY_FOR_TICKET && 
                observation.status != ObservationStatus.PENDING_TICKET) {
                return Result.Error("Observation is not ready for ticket issuance")
            }
            
            // Get related data
            val street = when (val result = streetRepository.getStreetById(observation.streetId)) {
                is Result.Success -> result.data
                is Result.Error -> return Result.Error("Street not found: ${result.message}")
            }
            
            val contravention = when (val result = contraventionRepository.getContraventionById(observation.contraventionId)) {
                is Result.Success -> result.data
                is Result.Error -> return Result.Error("Contravention not found: ${result.message}")
            }
            
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
                color = observation.vehicleColour ?: "Unknown",
                valvePositionFront = null, // TODO: Add valve position fields to observation
                valvePositionRear = null,
                observationId = observationId,
                issueTime = LocalDateTime.now(),
                penaltyAmount = penaltyAmount,
                discountAmount = discountAmount,
                photoPath = observation.photoFilename,
                notes = buildTicketNotes(observation, additionalNotes),
                status = TicketStatus.ISSUED,
                userId = currentUser.id,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            // Save ticket
            val ticketId = when (val result = ticketRepository.insertTicket(ticket)) {
                is Result.Success -> result.data
                is Result.Error -> return Result.Error("Failed to save ticket: ${result.message}")
            }
            
            // Update observation status
            val updatedObservation = observation.copy(
                status = ObservationStatus.TICKET_ISSUED,
                notes = "${observation.notes}\n\nTicket issued: $ticketNumber at ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}"
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
                is Result.Error -> Result.Error("Failed to update observation: ${result.message}")
            }
            
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to generate ticket")
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
        
        // Add observation notes
        if (observation.notes.isNotBlank()) {
            notes.append("Observation Notes:\n${observation.notes}\n\n")
        }
        
        // Add observation details
        notes.append("Observation Details:\n")
        notes.append("- Start Time: ${observation.countdownStartTime?.let { 
            LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it), java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        } ?: "N/A"}\n")
        notes.append("- End Time: ${observation.countdownEndTime?.let { 
            LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it), java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        } ?: "N/A"}\n")
        notes.append("- Location: ${observation.location}\n")
        
        // Add vehicle details
        if (observation.vehicleMake != null || observation.vehicleModel != null || observation.vehicleColour != null) {
            notes.append("\nVehicle Details:\n")
            observation.vehicleMake?.let { notes.append("- Make: $it\n") }
            observation.vehicleModel?.let { notes.append("- Model: $it\n") }
            observation.vehicleColour?.let { notes.append("- Colour: $it\n") }
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