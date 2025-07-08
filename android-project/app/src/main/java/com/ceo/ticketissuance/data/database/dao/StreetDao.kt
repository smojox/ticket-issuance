package com.ceo.ticketissuance.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ceo.ticketissuance.data.database.entity.StreetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreetDao {
    
    @Query("SELECT * FROM streets WHERE is_active = 1 ORDER BY name ASC")
    fun getActiveStreets(): Flow<List<StreetEntity>>
    
    @Query("SELECT * FROM streets ORDER BY name ASC")
    fun getAllStreets(): Flow<List<StreetEntity>>
    
    @Query("SELECT * FROM streets WHERE id = :id")
    suspend fun getStreetById(id: Long): StreetEntity?
    
    @Query("SELECT * FROM streets WHERE name = :name")
    suspend fun getStreetByName(name: String): StreetEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreet(street: StreetEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreets(streets: List<StreetEntity>)
    
    @Update
    suspend fun updateStreet(street: StreetEntity)
    
    @Delete
    suspend fun deleteStreet(street: StreetEntity)
    
    @Query("DELETE FROM streets")
    suspend fun deleteAllStreets()
}