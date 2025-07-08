package com.ceo.ticketissuance.domain.usecase

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.ObservationTimer
import com.ceo.ticketissuance.domain.repository.ObservationRepository
import javax.inject.Inject

class StartObservationTimerUseCase @Inject constructor(
    private val observationRepository: ObservationRepository
) {
    
    suspend operator fun invoke(
        observationId: Long,
        plateNumber: String,
        durationMinutes: Int = 10 // Default 10 minutes for most contraventions
    ): Result<ObservationTimer> {
        return try {
            val timer = ObservationTimer(
                observationId = observationId,
                plateNumber = plateNumber,
                durationMinutes = durationMinutes,
                remainingSeconds = durationMinutes * 60,
                isActive = true,
                isPaused = false,
                startTime = System.currentTimeMillis()
            )
            
            when (val result = observationRepository.updateObservationTimer(timer)) {
                is Result.Success -> Result.Success(timer)
                is Result.Error -> Result.Error(result.message)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to start observation timer")
        }
    }
}

class StopObservationTimerUseCase @Inject constructor(
    private val observationRepository: ObservationRepository
) {
    
    suspend operator fun invoke(observationId: Long): Result<Unit> {
        return try {
            when (val timerResult = observationRepository.getObservationTimer(observationId)) {
                is Result.Success -> {
                    val timer = timerResult.data
                    if (timer != null) {
                        val stoppedTimer = timer.copy(
                            isActive = false,
                            isPaused = false,
                            endTime = System.currentTimeMillis()
                        )
                        observationRepository.updateObservationTimer(stoppedTimer)
                    } else {
                        Result.Success(Unit)
                    }
                }
                is Result.Error -> Result.Error(timerResult.message)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to stop observation timer")
        }
    }
}

class PauseObservationTimerUseCase @Inject constructor(
    private val observationRepository: ObservationRepository
) {
    
    suspend operator fun invoke(observationId: Long): Result<Unit> {
        return try {
            when (val timerResult = observationRepository.getObservationTimer(observationId)) {
                is Result.Success -> {
                    val timer = timerResult.data
                    if (timer != null && timer.isActive) {
                        val pausedTimer = timer.copy(
                            isPaused = true,
                            pauseTime = System.currentTimeMillis()
                        )
                        observationRepository.updateObservationTimer(pausedTimer)
                    } else {
                        Result.Error("Timer not active")
                    }
                }
                is Result.Error -> Result.Error(timerResult.message)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to pause observation timer")
        }
    }
}

class ResumeObservationTimerUseCase @Inject constructor(
    private val observationRepository: ObservationRepository
) {
    
    suspend operator fun invoke(observationId: Long): Result<Unit> {
        return try {
            when (val timerResult = observationRepository.getObservationTimer(observationId)) {
                is Result.Success -> {
                    val timer = timerResult.data
                    if (timer != null && timer.isActive && timer.isPaused) {
                        val resumedTimer = timer.copy(
                            isPaused = false,
                            pauseTime = null
                        )
                        observationRepository.updateObservationTimer(resumedTimer)
                    } else {
                        Result.Error("Timer not paused")
                    }
                }
                is Result.Error -> Result.Error(timerResult.message)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to resume observation timer")
        }
    }
}