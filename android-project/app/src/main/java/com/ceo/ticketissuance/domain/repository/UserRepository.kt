package com.ceo.ticketissuance.domain.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAllUsers(): Flow<List<User>>
    suspend fun getUserById(id: Long): Result<User>
    suspend fun getUserByUsername(username: String): Result<User>
    suspend fun authenticateUser(username: String, password: String): Result<User>
    suspend fun insertUser(user: User): Result<Long>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(user: User): Result<Unit>
}