package com.ceo.ticketissuance.domain.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.Street
import kotlinx.coroutines.flow.Flow

interface StreetRepository {
    fun getActiveStreets(): Flow<List<Street>>
    fun getAllStreets(): Flow<List<Street>>
    suspend fun getStreetById(id: Long): Result<Street>
    suspend fun getStreetByName(name: String): Result<Street>
    suspend fun insertStreet(street: Street): Result<Long>
    suspend fun insertStreets(streets: List<Street>): Result<Unit>
    suspend fun updateStreet(street: Street): Result<Unit>
    suspend fun deleteStreet(street: Street): Result<Unit>
}