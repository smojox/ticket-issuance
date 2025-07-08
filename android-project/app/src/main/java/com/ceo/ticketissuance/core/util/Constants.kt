package com.ceo.ticketissuance.core.util

object Constants {
    // Database
    const val DATABASE_NAME = "ceo_tickets_database"
    const val DATABASE_VERSION = 1
    
    // Authentication
    const val TEST_USERNAME = "Test"
    const val TEST_PASSWORD = "Test"
    
    // Ticket Numbers
    const val TICKET_PREFIX = "TMA"
    
    // Contravention Times (in minutes)
    const val CONTRAVENTION_01_TIME = 5
    const val CONTRAVENTION_02_TIME = 5
    const val CONTRAVENTION_06_TIME = 0
    const val CONTRAVENTION_07_TIME = 0
    const val CONTRAVENTION_12_TIME = 0
    const val CONTRAVENTION_16_TIME = 0
    const val CONTRAVENTION_19_TIME = 0
    const val CONTRAVENTION_23_TIME = 0
    const val CONTRAVENTION_25_TIME = 5
    const val CONTRAVENTION_30_TIME = 10
    
    // Penalties
    const val STANDARD_PENALTY = 70.00
    const val EARLY_PAYMENT_DISCOUNT = 0.5
    
    // Sync
    const val SYNC_RETRY_LIMIT = 3
    const val SYNC_RETRY_DELAY_MS = 1000L
    
    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "countdown_notifications"
    const val NOTIFICATION_CHANNEL_NAME = "Countdown Notifications"
    
    // Preferences
    const val PREFS_NAME = "ceo_ticket_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_USERNAME = "username"
    const val PREF_SESSION_TOKEN = "session_token"
}