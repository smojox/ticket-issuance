package com.ceo.ticketissuance.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ceo.ticketissuance.data.database.entity.ContraventionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContraventionDao {
    
    @Query("SELECT * FROM contraventions WHERE is_active = 1 ORDER BY code ASC")
    fun getActiveContraventions(): Flow<List<ContraventionEntity>>
    
    @Query("SELECT * FROM contraventions WHERE street_id = :streetId AND is_active = 1 ORDER BY code ASC")
    fun getContraventionsByStreet(streetId: Long): Flow<List<ContraventionEntity>>
    
    @Query("SELECT * FROM contraventions ORDER BY code ASC")
    fun getAllContraventions(): Flow<List<ContraventionEntity>>
    
    @Query("SELECT * FROM contraventions WHERE id = :id")
    suspend fun getContraventionById(id: Long): ContraventionEntity?
    
    @Query("SELECT * FROM contraventions WHERE code = :code")
    suspend fun getContraventionByCode(code: String): ContraventionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContravention(contravention: ContraventionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContraventions(contraventions: List<ContraventionEntity>)
    
    @Update
    suspend fun updateContravention(contravention: ContraventionEntity)
    
    @Delete
    suspend fun deleteContravention(contravention: ContraventionEntity)
    
    @Query("DELETE FROM contraventions")
    suspend fun deleteAllContraventions()
}