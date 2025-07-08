package com.ceo.ticketissuance.core.timer

import android.content.Context
import android.content.SharedPreferences
import com.ceo.ticketissuance.domain.model.ObservationTimer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerPersistenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "timer_persistence", 
        Context.MODE_PRIVATE
    )
    
    private val _persistedTimers = MutableStateFlow<List<ObservationTimer>>(emptyList())
    val persistedTimers: StateFlow<List<ObservationTimer>> = _persistedTimers.asStateFlow()
    
    init {
        loadPersistedTimers()
    }
    
    fun saveTimerState(timer: ObservationTimer) {
        val currentTimers = _persistedTimers.value.toMutableList()
        
        // Remove existing timer with same ID
        currentTimers.removeAll { it.observationId == timer.observationId }
        
        // Add updated timer if it's still active
        if (timer.isActive) {
            currentTimers.add(timer)
        }
        
        _persistedTimers.value = currentTimers
        persistTimers(currentTimers)
    }
    
    fun removeTimer(observationId: Long) {
        val currentTimers = _persistedTimers.value.toMutableList()
        currentTimers.removeAll { it.observationId == observationId }
        
        _persistedTimers.value = currentTimers
        persistTimers(currentTimers)
    }
    
    fun getPersistedTimer(observationId: Long): ObservationTimer? {
        return _persistedTimers.value.find { it.observationId == observationId }
    }
    
    fun restoreActiveTimers(): List<ObservationTimer> {
        val currentTime = System.currentTimeMillis()
        val restoredTimers = mutableListOf<ObservationTimer>()
        
        _persistedTimers.value.forEach { timer ->
            if (timer.isActive) {
                val timeSinceLastUpdate = currentTime - timer.startTime
                val totalElapsed = timer.elapsedSeconds + (timeSinceLastUpdate / 1000).toInt()
                val newRemaining = maxOf(0, (timer.durationMinutes * 60) - totalElapsed)
                
                val restoredTimer = timer.copy(
                    remainingSeconds = newRemaining,
                    isActive = newRemaining > 0,
                    endTime = if (newRemaining == 0) currentTime else null
                )
                
                restoredTimers.add(restoredTimer)
            }
        }
        
        // Update persisted state with restored timers
        _persistedTimers.value = restoredTimers
        persistTimers(restoredTimers)
        
        return restoredTimers
    }
    
    fun clearCompletedTimers() {
        val activeTimers = _persistedTimers.value.filter { it.isActive }
        _persistedTimers.value = activeTimers
        persistTimers(activeTimers)
    }
    
    fun clearAllTimers() {
        _persistedTimers.value = emptyList()
        persistTimers(emptyList())
    }
    
    private fun loadPersistedTimers() {
        try {
            val timersJson = prefs.getString("active_timers", null)
            if (timersJson != null) {
                val type = object : TypeToken<List<ObservationTimer>>() {}.type
                val loadedTimers: List<ObservationTimer> = gson.fromJson(timersJson, type)
                _persistedTimers.value = loadedTimers
            }
        } catch (e: Exception) {
            // Handle JSON parsing errors gracefully
            _persistedTimers.value = emptyList()
        }
    }
    
    private fun persistTimers(timers: List<ObservationTimer>) {
        try {
            val timersJson = gson.toJson(timers)
            prefs.edit().putString("active_timers", timersJson).apply()
        } catch (e: Exception) {
            // Handle serialization errors gracefully
        }
    }
    
    fun getTimerStats(): TimerPersistenceStats {
        val timers = _persistedTimers.value
        
        return TimerPersistenceStats(
            totalPersisted = timers.size,
            activeCount = timers.count { it.isActive },
            pausedCount = timers.count { it.isPaused },
            completedCount = timers.count { it.isCompleted },
            oldestTimerAge = timers.minOfOrNull { 
                System.currentTimeMillis() - it.startTime 
            } ?: 0L
        )
    }
}

data class TimerPersistenceStats(
    val totalPersisted: Int,
    val activeCount: Int,
    val pausedCount: Int,
    val completedCount: Int,
    val oldestTimerAge: Long
) {
    val oldestTimerAgeHours: Float get() = oldestTimerAge / (1000f * 60f * 60f)
    val hasOldTimers: Boolean get() = oldestTimerAgeHours > 24f
}