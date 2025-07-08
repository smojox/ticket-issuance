package com.ceo.ticketissuance.domain.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.Contravention
import kotlinx.coroutines.flow.Flow

interface ContraventionRepository {
    fun getActiveContraventions(): Flow<List<Contravention>>
    fun getContraventionsByStreet(streetId: Long): Flow<List<Contravention>>
    fun getAllContraventions(): Flow<List<Contravention>>
    suspend fun getContraventionById(id: Long): Result<Contravention>
    suspend fun getContraventionByCode(code: String): Result<Contravention>
    suspend fun insertContravention(contravention: Contravention): Result<Long>
    suspend fun insertContraventions(contraventions: List<Contravention>): Result<Unit>
    suspend fun updateContravention(contravention: Contravention): Result<Unit>
    suspend fun deleteContravention(contravention: Contravention): Result<Unit>
}