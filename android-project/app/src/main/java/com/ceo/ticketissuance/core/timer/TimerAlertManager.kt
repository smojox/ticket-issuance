package com.ceo.ticketissuance.core.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ceo.ticketissuance.MainActivity
import com.ceo.ticketissuance.R
import com.ceo.ticketissuance.domain.model.ObservationTimer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerAlertManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        private const val ALERT_CHANNEL_ID = "timer_alerts"
        private const val ALERT_CHANNEL_NAME = "Timer Alerts"
        private const val WARNING_CHANNEL_ID = "timer_warnings"
        private const val WARNING_CHANNEL_NAME = "Timer Warnings"
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Alert channel for critical notifications
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                ALERT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical timer alerts"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            
            // Warning channel for less critical notifications
            val warningChannel = NotificationChannel(
                WARNING_CHANNEL_ID,
                WARNING_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Timer warnings and reminders"
                enableVibration(false)
                enableLights(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(alertChannel)
            notificationManager.createNotificationChannel(warningChannel)
        }
    }
    
    fun checkAndSendAlerts(timer: ObservationTimer) {
        when {
            timer.remainingSeconds <= 0 -> sendTimerExpiredAlert(timer)
            timer.remainingSeconds <= 60 -> sendCriticalAlert(timer)
            timer.remainingSeconds <= 300 -> sendWarningAlert(timer)
            timer.remainingSeconds <= 600 -> sendReminderAlert(timer)
        }
    }
    
    private fun sendTimerExpiredAlert(timer: ObservationTimer) {
        val intent = createTimerIntent(timer.observationId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            timer.observationId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setContentTitle("⏰ Timer Expired!")
            .setContentText("${timer.plateNumber} observation period complete")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.RED)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("The observation period for ${timer.plateNumber} has expired. The vehicle may now be issued with a penalty charge notice.")
            )
            .build()
        
        notificationManager.notify(getAlertNotificationId(timer.observationId, AlertType.EXPIRED), notification)
    }
    
    private fun sendCriticalAlert(timer: ObservationTimer) {
        val intent = createTimerIntent(timer.observationId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            timer.observationId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setContentTitle("🚨 Critical: ${timer.formatRemainingTime()}")
            .setContentText("${timer.plateNumber} timer expiring soon")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.RED)
            .setOnlyAlertOnce(true)
            .build()
        
        notificationManager.notify(getAlertNotificationId(timer.observationId, AlertType.CRITICAL), notification)
    }
    
    private fun sendWarningAlert(timer: ObservationTimer) {
        val intent = createTimerIntent(timer.observationId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            timer.observationId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
            .setContentTitle("⚠️ Warning: ${timer.formatRemainingTime()}")
            .setContentText("${timer.plateNumber} timer approaching end")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#FF9800")) // Orange
            .setOnlyAlertOnce(true)
            .build()
        
        notificationManager.notify(getAlertNotificationId(timer.observationId, AlertType.WARNING), notification)
    }
    
    private fun sendReminderAlert(timer: ObservationTimer) {
        val intent = createTimerIntent(timer.observationId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            timer.observationId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
            .setContentTitle("📋 Reminder: ${timer.formatRemainingTime()}")
            .setContentText("${timer.plateNumber} observation in progress")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#2196F3")) // Blue
            .setOnlyAlertOnce(true)
            .build()
        
        notificationManager.notify(getAlertNotificationId(timer.observationId, AlertType.REMINDER), notification)
    }
    
    fun sendMultipleTimerAlert(timers: List<ObservationTimer>) {
        if (timers.isEmpty()) return
        
        val criticalCount = timers.count { it.remainingSeconds <= 60 }
        val warningCount = timers.count { it.remainingSeconds <= 300 }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigation_target", "timer_management")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            999,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = when {
            criticalCount > 0 -> "🚨 $criticalCount Critical Timers"
            warningCount > 0 -> "⚠️ $warningCount Warning Timers"
            else -> "📋 ${timers.size} Active Timers"
        }
        
        val text = buildString {
            if (criticalCount > 0) {
                append("$criticalCount expiring soon")
                if (warningCount > 0) append(", ")
            }
            if (warningCount > 0) {
                append("$warningCount approaching end")
            }
        }
        
        val channelId = if (criticalCount > 0) ALERT_CHANNEL_ID else WARNING_CHANNEL_ID
        val priority = if (criticalCount > 0) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(priority)
            .setAutoCancel(true)
            .setColor(if (criticalCount > 0) android.graphics.Color.RED else android.graphics.Color.parseColor("#FF9800"))
            .setGroup("timer_alerts")
            .setGroupSummary(true)
            .build()
        
        notificationManager.notify(1000, notification)
    }
    
    fun cancelAlert(observationId: Long, alertType: AlertType) {
        notificationManager.cancel(getAlertNotificationId(observationId, alertType))
    }
    
    fun cancelAllAlertsForTimer(observationId: Long) {
        AlertType.values().forEach { type ->
            cancelAlert(observationId, type)
        }
    }
    
    private fun createTimerIntent(observationId: Long): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigation_target", "countdown")
            putExtra("observation_id", observationId)
        }
    }
    
    private fun getAlertNotificationId(observationId: Long, alertType: AlertType): Int {
        return (observationId * 100 + alertType.ordinal).toInt()
    }
    
    enum class AlertType {
        REMINDER,
        WARNING,
        CRITICAL,
        EXPIRED
    }
}

// Extension function to easily send alerts
fun ObservationTimer.sendAlertsIfNeeded(alertManager: TimerAlertManager) {
    alertManager.checkAndSendAlerts(this)
}