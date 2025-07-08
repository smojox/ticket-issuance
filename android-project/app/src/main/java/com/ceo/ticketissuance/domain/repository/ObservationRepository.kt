package com.ceo.ticketissuance.domain.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.Observation
import com.ceo.ticketissuance.domain.model.ObservationStatus
import com.ceo.ticketissuance.domain.model.ObservationTimer
import kotlinx.coroutines.flow.Flow

interface ObservationRepository {
    fun getAllObservations(): Flow<List<Observation>>
    fun getObservationsByStatus(status: ObservationStatus): Flow<List<Observation>>
    fun getObservationsByUser(userId: Long): Flow<List<Observation>>
    fun getObservationsByVrm(vrm: String): Flow<List<Observation>>
    fun getActiveObservations(): Flow<List<Observation>>
    suspend fun getObservationById(id: Long): Result<Observation>
    suspend fun insertObservation(observation: Observation): Result<Long>
    suspend fun insertObservations(observations: List<Observation>): Result<Unit>
    suspend fun updateObservation(observation: Observation): Result<Unit>
    suspend fun updateObservationStatus(id: Long, status: ObservationStatus): Result<Unit>
    suspend fun deleteObservation(observation: Observation): Result<Unit>
    
    // Timer methods
    suspend fun updateObservationTimer(timer: ObservationTimer): Result<Unit>
    suspend fun getObservationTimer(observationId: Long): Result<ObservationTimer?>
    fun getActiveTimers(): Flow<List<ObservationTimer>>
}