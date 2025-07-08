package com.ceo.ticketissuance.di

import android.content.Context
import androidx.room.Room
import com.ceo.ticketissuance.core.util.Constants
import com.ceo.ticketissuance.data.database.AppDatabase
import com.ceo.ticketissuance.data.database.dao.ContraventionDao
import com.ceo.ticketissuance.data.database.dao.ObservationDao
import com.ceo.ticketissuance.data.database.dao.StreetDao
import com.ceo.ticketissuance.data.database.dao.SyncQueueDao
import com.ceo.ticketissuance.data.database.dao.TicketDao
import com.ceo.ticketissuance.data.database.dao.UserDao
import com.ceo.ticketissuance.data.database.dao.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideStreetDao(database: AppDatabase): StreetDao = database.streetDao()

    @Provides
    fun provideContraventionDao(database: AppDatabase): ContraventionDao = database.contraventionDao()

    @Provides
    fun provideVehicleDao(database: AppDatabase): VehicleDao = database.vehicleDao()

    @Provides
    fun provideObservationDao(database: AppDatabase): ObservationDao = database.observationDao()

    @Provides
    fun provideTicketDao(database: AppDatabase): TicketDao = database.ticketDao()

    @Provides
    fun provideSyncQueueDao(database: AppDatabase): SyncQueueDao = database.syncQueueDao()
}