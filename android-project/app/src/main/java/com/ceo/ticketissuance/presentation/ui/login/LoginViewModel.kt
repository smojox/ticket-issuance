package com.ceo.ticketissuance.presentation.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ceo.ticketissuance.core.session.SessionManager
import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.usecase.AuthenticateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authenticateUserUseCase: AuthenticateUserUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun onLoginClick() {
        val currentState = _uiState.value
        
        // Clear previous errors
        _uiState.value = currentState.copy(
            usernameError = null,
            passwordError = null,
            generalError = null
        )

        // Validate inputs
        val usernameError = validateUsername(currentState.username)
        val passwordError = validatePassword(currentState.password)

        if (usernameError != null || passwordError != null) {
            _uiState.value = _uiState.value.copy(
                usernameError = usernameError,
                passwordError = passwordError
            )
            return
        }

        // Perform authentication
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            when (val result = authenticateUserUseCase(currentState.username, currentState.password)) {
                is Result.Success -> {
                    sessionManager.login(result.data)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(LoginEvent.NavigateToDashboard)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = result.exception.message ?: "Authentication failed"
                    )
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    private fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "Username is required"
            username.trim() != "Test" -> "Invalid username"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password != "Test" -> "Invalid password"
            else -> null
        }
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

sealed class LoginEvent {
    object NavigateToDashboard : LoginEvent()
}