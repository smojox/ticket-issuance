package com.ceo.ticketissuance.data.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.data.database.dao.StreetDao
import com.ceo.ticketissuance.data.mapper.toDomain
import com.ceo.ticketissuance.data.mapper.toEntity
import com.ceo.ticketissuance.domain.model.Street
import com.ceo.ticketissuance.domain.repository.StreetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreetRepositoryImpl @Inject constructor(
    private val streetDao: StreetDao
) : StreetRepository {

    override fun getActiveStreets(): Flow<List<Street>> {
        return streetDao.getActiveStreets().map { it.toDomain() }
    }

    override fun getAllStreets(): Flow<List<Street>> {
        return streetDao.getAllStreets().map { it.toDomain() }
    }

    override suspend fun getStreetById(id: Long): Result<Street> {
        return try {
            val street = streetDao.getStreetById(id)
            if (street != null) {
                Result.Success(street.toDomain())
            } else {
                Result.Error(Exception("Street not found with id: $id"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getStreetByName(name: String): Result<Street> {
        return try {
            val street = streetDao.getStreetByName(name)
            if (street != null) {
                Result.Success(street.toDomain())
            } else {
                Result.Error(Exception("Street not found with name: $name"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun insertStreet(street: Street): Result<Long> {
        return try {
            val id = streetDao.insertStreet(street.toEntity())
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun insertStreets(streets: List<Street>): Result<Unit> {
        return try {
            streetDao.insertStreets(streets.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateStreet(street: Street): Result<Unit> {
        return try {
            streetDao.updateStreet(street.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteStreet(street: Street): Result<Unit> {
        return try {
            streetDao.deleteStreet(street.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}