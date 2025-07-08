package com.ceo.ticketissuance.domain.model

data class ObservationTimer(
    val observationId: Long,
    val plateNumber: String,
    val durationMinutes: Int,
    val remainingSeconds: Int,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val pauseTime: Long? = null
) {
    val isCompleted: Boolean get() = remainingSeconds == 0 && !isActive
    val isRunning: Boolean get() = isActive && !isPaused
    val elapsedSeconds: Int get() = (durationMinutes * 60) - remainingSeconds
    val progressPercentage: Float get() = if (durationMinutes * 60 > 0) {
        elapsedSeconds.toFloat() / (durationMinutes * 60).toFloat()
    } else 0f
    
    fun formatRemainingTime(): String {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    fun formatElapsedTime(): String {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}