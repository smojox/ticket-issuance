package com.ceo.ticketissuance.domain.usecase

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.User
import com.ceo.ticketissuance.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return when {
            username.isBlank() -> Result.Error(Exception("Username cannot be empty"))
            password.isBlank() -> Result.Error(Exception("Password cannot be empty"))
            else -> userRepository.authenticateUser(username.trim(), password)
        }
    }
}