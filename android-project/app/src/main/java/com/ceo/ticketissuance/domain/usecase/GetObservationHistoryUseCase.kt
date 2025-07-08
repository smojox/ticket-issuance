package com.ceo.ticketissuance.domain.usecase

import com.ceo.ticketissuance.core.session.SessionManager
import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.Observation
import com.ceo.ticketissuance.domain.model.ObservationStatus
import com.ceo.ticketissuance.domain.repository.ObservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetObservationHistoryUseCase @Inject constructor(
    private val observationRepository: ObservationRepository,
    private val sessionManager: SessionManager
) {
    
    fun getAllObservations(): Flow<List<Observation>> {
        return observationRepository.getAllObservations()
    }
    
    fun getObservationsByStatus(status: ObservationStatus): Flow<List<Observation>> {
        return observationRepository.getObservationsByStatus(status)
    }
    
    fun getCurrentUserObservations(): Flow<List<Observation>> {
        return observationRepository.getAllObservations().map { observations ->
            val currentUser = sessionManager.getCurrentUser()
            if (currentUser != null) {
                observations.filter { it.userId == currentUser.id }
            } else {
                emptyList()
            }
        }
    }
    
    fun getObservationsForPlate(plateNumber: String): Flow<List<Observation>> {
        return observationRepository.getObservationsByVrm(plateNumber)
    }
    
    fun getActiveObservations(): Flow<List<Observation>> {
        return observationRepository.getActiveObservations()
    }
    
    suspend fun getObservationById(id: Long): Result<Observation> {
        return observationRepository.getObservationById(id)
    }
    
    fun getTodaysObservations(): Flow<List<Observation>> {
        return observationRepository.getAllObservations().map { observations ->
            val todayStart = getTodayStartTime()
            val todayEnd = getTodayEndTime()
            
            observations.filter { observation ->
                observation.observationTime in todayStart..todayEnd
            }
        }
    }
    
    fun getObservationStats(): Flow<ObservationStats> {
        return observationRepository.getAllObservations().map { observations ->
            val currentUser = sessionManager.getCurrentUser()
            val userObservations = if (currentUser != null) {
                observations.filter { it.userId == currentUser.id }
            } else {
                emptyList()
            }
            
            val todayStart = getTodayStartTime()
            val todayObservations = userObservations.filter { it.observationTime >= todayStart }
            
            val statusCounts = userObservations.groupingBy { it.status }.eachCount()
            
            ObservationStats(
                totalObservations = userObservations.size,
                todayObservations = todayObservations.size,
                activeObservations = statusCounts[ObservationStatus.ACTIVE] ?: 0,
                completedObservations = statusCounts[ObservationStatus.COMPLETED] ?: 0,
                ticketedObservations = statusCounts[ObservationStatus.TICKETED] ?: 0,
                cancelledObservations = statusCounts[ObservationStatus.CANCELLED] ?: 0
            )
        }
    }
    
    private fun getTodayStartTime(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getTodayEndTime(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}

data class ObservationStats(
    val totalObservations: Int,
    val todayObservations: Int,
    val activeObservations: Int,
    val completedObservations: Int,
    val ticketedObservations: Int,
    val cancelledObservations: Int
) {
    val completionRate: Float get() = if (totalObservations > 0) {
        (completedObservations + ticketedObservations).toFloat() / totalObservations.toFloat()
    } else 0f
    
    val ticketingRate: Float get() = if (totalObservations > 0) {
        ticketedObservations.toFloat() / totalObservations.toFloat()
    } else 0f
}