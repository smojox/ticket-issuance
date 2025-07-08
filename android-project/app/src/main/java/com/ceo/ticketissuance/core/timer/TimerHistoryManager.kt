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
class TimerHistoryManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "timer_history", 
        Context.MODE_PRIVATE
    )
    
    private val _timerHistory = MutableStateFlow<List<TimerHistoryEntry>>(emptyList())
    val timerHistory: StateFlow<List<TimerHistoryEntry>> = _timerHistory.asStateFlow()
    
    init {
        loadHistory()
    }
    
    fun logTimerEvent(
        observationId: Long,
        plateNumber: String,
        event: TimerEvent,
        duration: Int = 0,
        remainingTime: Int = 0
    ) {
        val entry = TimerHistoryEntry(
            id = System.currentTimeMillis(),
            observationId = observationId,
            plateNumber = plateNumber,
            event = event,
            timestamp = System.currentTimeMillis(),
            duration = duration,
            remainingTime = remainingTime
        )
        
        val currentHistory = _timerHistory.value.toMutableList()
        currentHistory.add(0, entry) // Add to beginning
        
        // Keep only last 100 entries
        if (currentHistory.size > 100) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        _timerHistory.value = currentHistory
        persistHistory(currentHistory)
    }
    
    fun getHistoryForObservation(observationId: Long): List<TimerHistoryEntry> {
        return _timerHistory.value.filter { it.observationId == observationId }
    }
    
    fun getHistoryForPlate(plateNumber: String): List<TimerHistoryEntry> {
        return _timerHistory.value.filter { it.plateNumber == plateNumber }
    }
    
    fun getRecentHistory(hours: Int = 24): List<TimerHistoryEntry> {
        val cutoffTime = System.currentTimeMillis() - (hours * 60 * 60 * 1000)
        return _timerHistory.value.filter { it.timestamp >= cutoffTime }
    }
    
    fun clearHistory() {
        _timerHistory.value = emptyList()
        persistHistory(emptyList())
    }
    
    fun clearOldHistory(daysToKeep: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000)
        val recentHistory = _timerHistory.value.filter { it.timestamp >= cutoffTime }
        
        _timerHistory.value = recentHistory
        persistHistory(recentHistory)
    }
    
    fun getHistoryStats(): TimerHistoryStats {
        val history = _timerHistory.value
        val last24Hours = getRecentHistory(24)
        
        return TimerHistoryStats(
            totalEntries = history.size,
            entriesLast24Hours = last24Hours.size,
            timersStarted = history.count { it.event == TimerEvent.STARTED },
            timersCompleted = history.count { it.event == TimerEvent.COMPLETED },
            timersCancelled = history.count { it.event == TimerEvent.STOPPED },
            timersPaused = history.count { it.event == TimerEvent.PAUSED },
            averageDuration = history.filter { it.event == TimerEvent.COMPLETED }
                .map { it.duration }.average().takeIf { !it.isNaN() }?.toInt() ?: 0,
            mostActiveHour = findMostActiveHour(history),
            uniquePlates = history.map { it.plateNumber }.distinct().size
        )
    }
    
    private fun findMostActiveHour(history: List<TimerHistoryEntry>): Int {
        val hourCounts = mutableMapOf<Int, Int>()
        
        history.forEach { entry ->
            val hour = java.util.Calendar.getInstance().apply {
                timeInMillis = entry.timestamp
            }.get(java.util.Calendar.HOUR_OF_DAY)
            
            hourCounts[hour] = hourCounts.getOrDefault(hour, 0) + 1
        }
        
        return hourCounts.maxByOrNull { it.value }?.key ?: 0
    }
    
    private fun loadHistory() {
        try {
            val historyJson = prefs.getString("timer_history", null)
            if (historyJson != null) {
                val type = object : TypeToken<List<TimerHistoryEntry>>() {}.type
                val loadedHistory: List<TimerHistoryEntry> = gson.fromJson(historyJson, type)
                _timerHistory.value = loadedHistory
            }
        } catch (e: Exception) {
            _timerHistory.value = emptyList()
        }
    }
    
    private fun persistHistory(history: List<TimerHistoryEntry>) {
        try {
            val historyJson = gson.toJson(history)
            prefs.edit().putString("timer_history", historyJson).apply()
        } catch (e: Exception) {
            // Handle serialization errors gracefully
        }
    }
}

data class TimerHistoryEntry(
    val id: Long,
    val observationId: Long,
    val plateNumber: String,
    val event: TimerEvent,
    val timestamp: Long,
    val duration: Int, // In minutes
    val remainingTime: Int // In seconds
) {
    val formattedTime: String
        get() {
            val dateFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            return dateFormat.format(java.util.Date(timestamp))
        }
    
    val formattedDate: String
        get() {
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            return dateFormat.format(java.util.Date(timestamp))
        }
}

enum class TimerEvent {
    STARTED,
    PAUSED,
    RESUMED,
    STOPPED,
    COMPLETED,
    EXPIRED
}

data class TimerHistoryStats(
    val totalEntries: Int,
    val entriesLast24Hours: Int,
    val timersStarted: Int,
    val timersCompleted: Int,
    val timersCancelled: Int,
    val timersPaused: Int,
    val averageDuration: Int,
    val mostActiveHour: Int,
    val uniquePlates: Int
) {
    val completionRate: Float
        get() = if (timersStarted > 0) {
            timersCompleted.toFloat() / timersStarted.toFloat()
        } else 0f
    
    val cancellationRate: Float
        get() = if (timersStarted > 0) {
            timersCancelled.toFloat() / timersStarted.toFloat()
        } else 0f
}