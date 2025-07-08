package com.ceo.ticketissuance.domain.repository

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.VehicleMake
import com.ceo.ticketissuance.domain.model.VehicleModel
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    // Vehicle Makes
    fun getActiveMakes(): Flow<List<VehicleMake>>
    fun getAllMakes(): Flow<List<VehicleMake>>
    fun getAllVehicleMakes(): Flow<List<VehicleMake>> // Alias for consistency
    suspend fun getMakeById(id: Long): Result<VehicleMake>
    suspend fun insertMake(make: VehicleMake): Result<Long>
    suspend fun insertMakes(makes: List<VehicleMake>): Result<Unit>
    suspend fun updateMake(make: VehicleMake): Result<Unit>
    suspend fun deleteMake(make: VehicleMake): Result<Unit>
    
    // Vehicle Models
    fun getActiveModels(): Flow<List<VehicleModel>>
    fun getModelsByMake(makeId: Long): Flow<List<VehicleModel>>
    fun getVehicleModelsByMake(makeId: Long): Flow<List<VehicleModel>> // Alias for consistency
    fun getAllModels(): Flow<List<VehicleModel>>
    suspend fun getModelById(id: Long): Result<VehicleModel>
    suspend fun insertModel(model: VehicleModel): Result<Long>
    suspend fun insertModels(models: List<VehicleModel>): Result<Unit>
    suspend fun updateModel(model: VehicleModel): Result<Unit>
    suspend fun deleteModel(model: VehicleModel): Result<Unit>
}