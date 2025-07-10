package com.ceo.ticketissuance.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ceo.ticketissuance.data.database.converter.Converters
import com.ceo.ticketissuance.data.database.dao.ContraventionDao
import com.ceo.ticketissuance.data.database.dao.ObservationDao
import com.ceo.ticketissuance.data.database.dao.StreetDao
import com.ceo.ticketissuance.data.database.dao.SyncQueueDao
import com.ceo.ticketissuance.data.database.dao.TicketDao
import com.ceo.ticketissuance.data.database.dao.UserDao
import com.ceo.ticketissuance.data.database.dao.VehicleDao
import com.ceo.ticketissuance.data.database.entity.ContraventionEntity
import com.ceo.ticketissuance.data.database.entity.ObservationEntity
import com.ceo.ticketissuance.data.database.entity.StreetEntity
import com.ceo.ticketissuance.data.database.entity.SyncQueueEntity
import com.ceo.ticketissuance.data.database.entity.TicketEntity
import com.ceo.ticketissuance.data.database.entity.UserEntity
import com.ceo.ticketissuance.data.database.entity.VehicleMakeEntity
import com.ceo.ticketissuance.data.database.entity.VehicleModelEntity

@Database(
    entities = [
        UserEntity::class,
        StreetEntity::class,
        ContraventionEntity::class,
        VehicleMakeEntity::class,
        VehicleModelEntity::class,
        ObservationEntity::class,
        TicketEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun streetDao(): StreetDao
    abstract fun contraventionDao(): ContraventionDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun observationDao(): ObservationDao
    abstract fun ticketDao(): TicketDao
    abstract fun syncQueueDao(): SyncQueueDao
    
    companion object {
        const val DATABASE_NAME = "ceo_tickets_database"
        
        // Future migrations can be added here
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration - add new column to tickets table
                // database.execSQL("ALTER TABLE tickets ADD COLUMN new_column TEXT")
            }
        }
    }
}