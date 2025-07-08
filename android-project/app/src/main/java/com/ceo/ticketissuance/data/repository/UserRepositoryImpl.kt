package com.ceo.ticketissuance.data.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.data.database.dao.UserDao
import com.ceo.ticketissuance.data.mapper.toDomain
import com.ceo.ticketissuance.data.mapper.toEntity
import com.ceo.ticketissuance.domain.model.User
import com.ceo.ticketissuance.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { it.toDomain() }
    }

    override suspend fun getUserById(id: Long): Result<User> {
        return try {
            val user = userDao.getUserById(id)
            if (user != null) {
                Result.Success(user.toDomain())
            } else {
                Result.Error(Exception("User not found with id: $id"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getUserByUsername(username: String): Result<User> {
        return try {
            val user = userDao.getUserByUsername(username)
            if (user != null) {
                Result.Success(user.toDomain())
            } else {
                Result.Error(Exception("User not found with username: $username"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun authenticateUser(username: String, password: String): Result<User> {
        return try {
            val user = userDao.authenticateUser(username, password)
            if (user != null) {
                Result.Success(user.toDomain())
            } else {
                Result.Error(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun insertUser(user: User): Result<Long> {
        return try {
            val id = userDao.insertUser(user.toEntity())
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            userDao.updateUser(user.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteUser(user: User): Result<Unit> {
        return try {
            userDao.deleteUser(user.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}