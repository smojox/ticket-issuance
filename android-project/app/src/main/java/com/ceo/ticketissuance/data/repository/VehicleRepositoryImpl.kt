package com.ceo.ticketissuance.data.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.data.database.dao.VehicleDao
import com.ceo.ticketissuance.data.mapper.makesToDomain
import com.ceo.ticketissuance.data.mapper.modelsToDomain
import com.ceo.ticketissuance.domain.model.VehicleMake
import com.ceo.ticketissuance.domain.model.VehicleModel
import com.ceo.ticketissuance.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao
) : VehicleRepository {

    override fun getActiveMakes(): Flow<List<VehicleMake>> {
        return vehicleDao.getActiveMakes().map { it.makesToDomain() }
    }

    override fun getAllMakes(): Flow<List<VehicleMake>> {
        return vehicleDao.getAllMakes().map { it.makesToDomain() }
    }
    
    override fun getAllVehicleMakes(): Flow<List<VehicleMake>> {
        return getAllMakes() // Alias implementation
    }

    override fun getActiveModels(): Flow<List<VehicleModel>> {
        return vehicleDao.getActiveModels().map { it.modelsToDomain() }
    }

    override fun getModelsByMake(makeId: Long): Flow<List<VehicleModel>> {
        return vehicleDao.getModelsByMake(makeId).map { it.modelsToDomain() }
    }
    
    override fun getVehicleModelsByMake(makeId: Long): Flow<List<VehicleModel>> {
        return getModelsByMake(makeId) // Alias implementation
    }

    override fun getAllModels(): Flow<List<VehicleModel>> {
        return vehicleDao.getAllModels().map { it.modelsToDomain() }
    }

    // Placeholder implementations for other methods
    override suspend fun getMakeById(id: Long): Result<VehicleMake> {
        TODO("Implement in next phase")
    }

    override suspend fun insertMake(make: VehicleMake): Result<Long> {
        TODO("Implement in next phase")
    }

    override suspend fun insertMakes(makes: List<VehicleMake>): Result<Unit> {
        TODO("Implement in next phase")
    }

    override suspend fun updateMake(make: VehicleMake): Result<Unit> {
        TODO("Implement in next phase")
    }

    override suspend fun deleteMake(make: VehicleMake): Result<Unit> {
        TODO("Implement in next phase")
    }

    override suspend fun getModelById(id: Long): Result<VehicleModel> {
        TODO("Implement in next phase")
    }

    override suspend fun insertModel(model: VehicleModel): Result<Long> {
        TODO("Implement in next phase")
    }

    override suspend fun insertModels(models: List<VehicleModel>): Result<Unit> {
        TODO("Implement in next phase")
    }

    override suspend fun updateModel(model: VehicleModel): Result<Unit> {
        TODO("Implement in next phase")
    }

    override suspend fun deleteModel(model: VehicleModel): Result<Unit> {
        TODO("Implement in next phase")
    }
}