package com.ceo.ticketissuance.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ceo.ticketissuance.data.database.entity.VehicleMakeEntity
import com.ceo.ticketissuance.data.database.entity.VehicleModelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    
    // Vehicle Makes
    @Query("SELECT * FROM vehicle_makes WHERE is_active = 1 ORDER BY name ASC")
    fun getActiveMakes(): Flow<List<VehicleMakeEntity>>
    
    @Query("SELECT * FROM vehicle_makes ORDER BY name ASC")
    fun getAllMakes(): Flow<List<VehicleMakeEntity>>
    
    @Query("SELECT * FROM vehicle_makes WHERE id = :id")
    suspend fun getMakeById(id: Long): VehicleMakeEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMake(make: VehicleMakeEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMakes(makes: List<VehicleMakeEntity>)
    
    @Update
    suspend fun updateMake(make: VehicleMakeEntity)
    
    @Delete
    suspend fun deleteMake(make: VehicleMakeEntity)
    
    // Vehicle Models
    @Query("SELECT * FROM vehicle_models WHERE is_active = 1 ORDER BY name ASC")
    fun getActiveModels(): Flow<List<VehicleModelEntity>>
    
    @Query("SELECT * FROM vehicle_models WHERE make_id = :makeId AND is_active = 1 ORDER BY name ASC")
    fun getModelsByMake(makeId: Long): Flow<List<VehicleModelEntity>>
    
    @Query("SELECT * FROM vehicle_models ORDER BY name ASC")
    fun getAllModels(): Flow<List<VehicleModelEntity>>
    
    @Query("SELECT * FROM vehicle_models WHERE id = :id")
    suspend fun getModelById(id: Long): VehicleModelEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: VehicleModelEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModels(models: List<VehicleModelEntity>)
    
    @Update
    suspend fun updateModel(model: VehicleModelEntity)
    
    @Delete
    suspend fun deleteModel(model: VehicleModelEntity)
    
    @Query("DELETE FROM vehicle_makes")
    suspend fun deleteAllMakes()
    
    @Query("DELETE FROM vehicle_models")
    suspend fun deleteAllModels()
}