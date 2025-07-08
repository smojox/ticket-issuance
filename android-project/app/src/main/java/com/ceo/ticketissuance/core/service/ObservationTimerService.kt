package com.ceo.ticketissuance.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ceo.ticketissuance.MainActivity
import com.ceo.ticketissuance.R
import com.ceo.ticketissuance.core.timer.TimerPersistenceManager
import com.ceo.ticketissuance.core.util.Constants
import com.ceo.ticketissuance.domain.model.ObservationTimer
import com.ceo.ticketissuance.domain.repository.ObservationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@AndroidEntryPoint
class ObservationTimerService : Service() {
    
    @Inject
    lateinit var observationRepository: ObservationRepository
    
    @Inject
    lateinit var timerPersistenceManager: TimerPersistenceManager
    
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val activeTimers = ConcurrentHashMap<Long, TimerJob>()
    
    private val _timerStates = MutableStateFlow<Map<Long, ObservationTimer>>(emptyMap())
    val timerStates: StateFlow<Map<Long, ObservationTimer>> = _timerStates.asStateFlow()
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "observation_timer_channel"
        private const val CHANNEL_NAME = "Observation Timers"
        
        const val ACTION_START_TIMER = "START_TIMER"
        const val ACTION_STOP_TIMER = "STOP_TIMER"
        const val ACTION_PAUSE_TIMER = "PAUSE_TIMER"
        const val ACTION_RESUME_TIMER = "RESUME_TIMER"
        
        const val EXTRA_OBSERVATION_ID = "observation_id"
        const val EXTRA_DURATION_MINUTES = "duration_minutes"
        const val EXTRA_PLATE_NUMBER = "plate_number"
        
        private var _instance: ObservationTimerService? = null
        val instance: ObservationTimerService? get() = _instance
    }
    
    override fun onCreate() {
        super.onCreate()
        _instance = this
        createNotificationChannel()
        restorePersistedTimers()
    }
    
    private fun restorePersistedTimers() {
        serviceScope.launch {
            val restoredTimers = timerPersistenceManager.restoreActiveTimers()
            restoredTimers.forEach { timer ->
                if (timer.isActive) {
                    startRestoredTimer(timer)
                }
            }
        }
    }
    
    private fun startRestoredTimer(timer: ObservationTimer) {
        val job = serviceScope.launch {
            runTimer(timer)
        }
        
        activeTimers[timer.observationId] = TimerJob(job, timer)
        updateTimerState(timer.observationId, timer)
        
        if (activeTimers.size == 1) {
            startForeground(NOTIFICATION_ID, createNotification(timer))
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _instance = null
        activeTimers.values.forEach { it.job.cancel() }
        activeTimers.clear()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val observationId = intent.getLongExtra(EXTRA_OBSERVATION_ID, -1L)
                val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 10)
                val plateNumber = intent.getStringExtra(EXTRA_PLATE_NUMBER) ?: ""
                
                if (observationId != -1L) {
                    startTimer(observationId, durationMinutes, plateNumber)
                }
            }
            ACTION_STOP_TIMER -> {
                val observationId = intent.getLongExtra(EXTRA_OBSERVATION_ID, -1L)
                if (observationId != -1L) {
                    stopTimer(observationId)
                }
            }
            ACTION_PAUSE_TIMER -> {
                val observationId = intent.getLongExtra(EXTRA_OBSERVATION_ID, -1L)
                if (observationId != -1L) {
                    pauseTimer(observationId)
                }
            }
            ACTION_RESUME_TIMER -> {
                val observationId = intent.getLongExtra(EXTRA_OBSERVATION_ID, -1L)
                if (observationId != -1L) {
                    resumeTimer(observationId)
                }
            }
        }
        
        return START_STICKY
    }
    
    private fun startTimer(observationId: Long, durationMinutes: Int, plateNumber: String) {
        // Stop existing timer if any
        stopTimer(observationId)
        
        val timer = ObservationTimer(
            observationId = observationId,
            plateNumber = plateNumber,
            durationMinutes = durationMinutes,
            remainingSeconds = durationMinutes * 60,
            isActive = true,
            isPaused = false,
            startTime = System.currentTimeMillis()
        )
        
        val job = serviceScope.launch {
            runTimer(timer)
        }
        
        activeTimers[observationId] = TimerJob(job, timer)
        updateTimerState(observationId, timer)
        
        startForeground(NOTIFICATION_ID, createNotification(timer))
    }
    
    private fun stopTimer(observationId: Long) {
        activeTimers[observationId]?.let { timerJob ->
            timerJob.job.cancel()
            activeTimers.remove(observationId)
            
            val updatedTimer = timerJob.timer.copy(
                isActive = false,
                isPaused = false,
                endTime = System.currentTimeMillis()
            )
            updateTimerState(observationId, updatedTimer)
            
            serviceScope.launch {
                observationRepository.updateObservationTimer(updatedTimer)
            }
        }
        
        if (activeTimers.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }
    
    private fun pauseTimer(observationId: Long) {
        activeTimers[observationId]?.let { timerJob ->
            val updatedTimer = timerJob.timer.copy(
                isPaused = true,
                pauseTime = System.currentTimeMillis()
            )
            activeTimers[observationId] = timerJob.copy(timer = updatedTimer)
            updateTimerState(observationId, updatedTimer)
        }
    }
    
    private fun resumeTimer(observationId: Long) {
        activeTimers[observationId]?.let { timerJob ->
            val updatedTimer = timerJob.timer.copy(
                isPaused = false,
                pauseTime = null
            )
            activeTimers[observationId] = timerJob.copy(timer = updatedTimer)
            updateTimerState(observationId, updatedTimer)
        }
    }
    
    private suspend fun runTimer(initialTimer: ObservationTimer) {
        var timer = initialTimer
        
        while (timer.isActive && timer.remainingSeconds > 0) {
            delay(1000) // 1 second
            
            val currentTimer = activeTimers[timer.observationId]?.timer
            if (currentTimer == null || !currentTimer.isActive) {
                break
            }
            
            if (!currentTimer.isPaused) {
                timer = currentTimer.copy(
                    remainingSeconds = maxOf(0, currentTimer.remainingSeconds - 1)
                )
                activeTimers[timer.observationId] = 
                    activeTimers[timer.observationId]!!.copy(timer = timer)
                updateTimerState(timer.observationId, timer)
                
                // Update notification
                val notification = createNotification(timer)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
        
        // Timer finished
        if (timer.remainingSeconds == 0) {
            val completedTimer = timer.copy(
                isActive = false,
                endTime = System.currentTimeMillis()
            )
            updateTimerState(timer.observationId, completedTimer)
            
            serviceScope.launch {
                observationRepository.updateObservationTimer(completedTimer)
            }
            
            // Remove from active timers
            activeTimers.remove(timer.observationId)
            
            // Show completion notification
            showTimerCompletedNotification(timer.plateNumber, timer.observationId)
            
            // If no more active timers, stop service
            if (activeTimers.isEmpty()) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }
    
    private fun updateTimerState(observationId: Long, timer: ObservationTimer) {
        val currentStates = _timerStates.value.toMutableMap()
        currentStates[observationId] = timer
        _timerStates.value = currentStates
        
        // Persist timer state
        timerPersistenceManager.saveTimerState(timer)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows observation timer progress"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(timer: ObservationTimer): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigation_target", "countdown")
            putExtra("observation_id", timer.observationId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, timer.observationId.toInt(), intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val minutes = timer.remainingSeconds / 60
        val seconds = timer.remainingSeconds % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)
        
        // Create action buttons
        val pauseIntent = Intent(this, ObservationTimerService::class.java).apply {
            action = if (timer.isPaused) ACTION_RESUME_TIMER else ACTION_PAUSE_TIMER
            putExtra(EXTRA_OBSERVATION_ID, timer.observationId)
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 
            (timer.observationId * 10 + 1).toInt(),
            pauseIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, ObservationTimerService::class.java).apply {
            action = ACTION_STOP_TIMER
            putExtra(EXTRA_OBSERVATION_ID, timer.observationId)
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 
            (timer.observationId * 10 + 2).toInt(),
            stopIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Observation Timer - ${timer.plateNumber}")
            .setContentText("$timeText remaining")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        
        // Add progress indicator
        val totalSeconds = timer.durationMinutes * 60
        val progress = ((totalSeconds - timer.remainingSeconds) * 100) / totalSeconds
        builder.setProgress(100, progress, false)
        
        // Add action buttons if timer is active
        if (timer.isActive) {
            builder.addAction(
                if (timer.isPaused) R.drawable.ic_launcher_foreground else R.drawable.ic_launcher_foreground,
                if (timer.isPaused) "Resume" else "Pause",
                pausePendingIntent
            )
            
            builder.addAction(
                R.drawable.ic_launcher_foreground,
                "Stop",
                stopPendingIntent
            )
        }
        
        // Change appearance based on remaining time
        when {
            timer.remainingSeconds <= 60 -> {
                builder.setColor(android.graphics.Color.RED)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
            }
            timer.remainingSeconds <= 300 -> {
                builder.setColor(android.graphics.Color.parseColor("#FF9800")) // Orange
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            }
            else -> {
                builder.setColor(android.graphics.Color.parseColor("#4CAF50")) // Green
                    .setPriority(NotificationCompat.PRIORITY_LOW)
            }
        }
        
        return builder.build()
    }
    
    private fun showTimerCompletedNotification(plateNumber: String, observationId: Long) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigation_target", "countdown")
            putExtra("observation_id", observationId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, observationId.toInt(), intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create action for issuing ticket
        val ticketIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigation_target", "issuance")
            putExtra("observation_id", observationId)
        }
        val ticketPendingIntent = PendingIntent.getActivity(
            this, (observationId * 10 + 3).toInt(), ticketIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏰ Observation Complete")
            .setContentText("$plateNumber - Ready for ticket issuance")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(android.graphics.Color.RED)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Issue Ticket",
                ticketPendingIntent
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Observation period for $plateNumber has expired. The vehicle may now be issued with a penalty charge notice.")
            )
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(observationId.toInt() + 10000, notification) // Unique ID for completion notifications
    }
    
    fun getTimerState(observationId: Long): ObservationTimer? {
        return activeTimers[observationId]?.timer
    }
    
    fun getAllActiveTimers(): List<ObservationTimer> {
        return activeTimers.values.map { it.timer }
    }
    
    private data class TimerJob(
        val job: Job,
        val timer: ObservationTimer
    )
}

// Extension function to start timer from anywhere in the app
fun Context.startObservationTimer(
    observationId: Long,
    durationMinutes: Int,
    plateNumber: String
) {
    val intent = Intent(this, ObservationTimerService::class.java).apply {
        action = ObservationTimerService.ACTION_START_TIMER
        putExtra(ObservationTimerService.EXTRA_OBSERVATION_ID, observationId)
        putExtra(ObservationTimerService.EXTRA_DURATION_MINUTES, durationMinutes)
        putExtra(ObservationTimerService.EXTRA_PLATE_NUMBER, plateNumber)
    }
    startForegroundService(intent)
}

fun Context.stopObservationTimer(observationId: Long) {
    val intent = Intent(this, ObservationTimerService::class.java).apply {
        action = ObservationTimerService.ACTION_STOP_TIMER
        putExtra(ObservationTimerService.EXTRA_OBSERVATION_ID, observationId)
    }
    startService(intent)
}

fun Context.pauseObservationTimer(observationId: Long) {
    val intent = Intent(this, ObservationTimerService::class.java).apply {
        action = ObservationTimerService.ACTION_PAUSE_TIMER
        putExtra(ObservationTimerService.EXTRA_OBSERVATION_ID, observationId)
    }
    startService(intent)
}

fun Context.resumeObservationTimer(observationId: Long) {
    val intent = Intent(this, ObservationTimerService::class.java).apply {
        action = ObservationTimerService.ACTION_RESUME_TIMER
        putExtra(ObservationTimerService.EXTRA_OBSERVATION_ID, observationId)
    }
    startService(intent)
}