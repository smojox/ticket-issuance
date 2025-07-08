package com.ceo.ticketissuance.core.session

import android.content.SharedPreferences
import com.ceo.ticketissuance.core.util.Constants
import com.ceo.ticketissuance.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val preferences: SharedPreferences
) {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    init {
        // Check for existing session on init
        checkExistingSession()
    }
    
    fun login(user: User) {
        val sessionToken = UUID.randomUUID().toString()
        
        preferences.edit().apply {
            putLong(Constants.PREF_USER_ID, user.id)
            putString(Constants.PREF_USERNAME, user.username)
            putString(Constants.PREF_SESSION_TOKEN, sessionToken)
            apply()
        }
        
        _currentUser.value = user
        _isLoggedIn.value = true
    }
    
    fun logout() {
        preferences.edit().apply {
            remove(Constants.PREF_USER_ID)
            remove(Constants.PREF_USERNAME)
            remove(Constants.PREF_SESSION_TOKEN)
            apply()
        }
        
        _currentUser.value = null
        _isLoggedIn.value = false
    }
    
    fun getCurrentUserId(): Long? {
        return _currentUser.value?.id
    }
    
    fun getCurrentUsername(): String? {
        return _currentUser.value?.username
    }
    
    private fun checkExistingSession() {
        val userId = preferences.getLong(Constants.PREF_USER_ID, -1L)
        val username = preferences.getString(Constants.PREF_USERNAME, null)
        val sessionToken = preferences.getString(Constants.PREF_SESSION_TOKEN, null)
        
        if (userId != -1L && username != null && sessionToken != null) {
            // For this phase, we'll just set the basic user info
            // In a real app, we'd validate the session token
            _isLoggedIn.value = true
            // Note: We're not setting _currentUser here as we'd need to fetch from database
            // This will be handled in the splash screen
        }
    }
    
    fun hasValidSession(): Boolean {
        return preferences.getLong(Constants.PREF_USER_ID, -1L) != -1L &&
               preferences.getString(Constants.PREF_SESSION_TOKEN, null) != null
    }
}