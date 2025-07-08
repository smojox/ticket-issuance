package com.ceo.ticketissuance.data.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.data.database.dao.ContraventionDao
import com.ceo.ticketissuance.data.mapper.toDomain
import com.ceo.ticketissuance.domain.model.Contravention
import com.ceo.ticketissuance.domain.repository.ContraventionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContraventionRepositoryImpl @Inject constructor(
    private val contraventionDao: ContraventionDao
) : ContraventionRepository {

    override fun getActiveContraventions(): Flow<List<Contravention>> {
        return contraventionDao.getActiveContraventions().map { it.toDomain() }
    }

    override fun getContraventionsByStreet(streetId: Long): Flow<List<Contravention>> {
        return contraventionDao.getContraventionsByStreet(streetId).map { it.toDomain() }
    }

    override fun getAllContraventions(): Flow<List<Contravention>> {
        return contraventionDao.getAllContraventions().map { it.toDomain() }
    }

    override suspend fun getContraventionById(id: Long): Result<Contravention> {
        // Implementation similar to other repositories
        TODO("Implement in next phase")
    }

    override suspend fun getContraventionByCode(code: String): Result<Contravention> {
        TODO("Implement in next phase")
    }

    override suspend fun insertContravention(contravention: Contravention): Result<Long> {
        TODO("Implement in next phase")
    }

    override suspend fun insertContraventions(contraventions: List<Contravention>): Result<Unit> {
        TODO("Implement in next phase")
    }

    override suspend fun updateContravention(contravention: Contravention): Result<Unit> {
        TODO("Implement in next phase")
    }

    override suspend fun deleteContravention(contravention: Contravention): Result<Unit> {
        TODO("Implement in next phase")
    }
}