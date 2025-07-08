package com.ceo.ticketissuance.di

import com.ceo.ticketissuance.data.repository.ContraventionRepositoryImpl
import com.ceo.ticketissuance.data.repository.ObservationRepositoryImpl
import com.ceo.ticketissuance.data.repository.StreetRepositoryImpl
import com.ceo.ticketissuance.data.repository.SyncQueueRepositoryImpl
import com.ceo.ticketissuance.data.repository.TicketRepositoryImpl
import com.ceo.ticketissuance.data.repository.UserRepositoryImpl
import com.ceo.ticketissuance.data.repository.VehicleRepositoryImpl
import com.ceo.ticketissuance.domain.repository.ContraventionRepository
import com.ceo.ticketissuance.domain.repository.ObservationRepository
import com.ceo.ticketissuance.domain.repository.StreetRepository
import com.ceo.ticketissuance.domain.repository.SyncQueueRepository
import com.ceo.ticketissuance.domain.repository.TicketRepository
import com.ceo.ticketissuance.domain.repository.UserRepository
import com.ceo.ticketissuance.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindStreetRepository(
        streetRepositoryImpl: StreetRepositoryImpl
    ): StreetRepository

    @Binds
    @Singleton
    abstract fun bindContraventionRepository(
        contraventionRepositoryImpl: ContraventionRepositoryImpl
    ): ContraventionRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        vehicleRepositoryImpl: VehicleRepositoryImpl
    ): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindObservationRepository(
        observationRepositoryImpl: ObservationRepositoryImpl
    ): ObservationRepository

    @Binds
    @Singleton
    abstract fun bindTicketRepository(
        ticketRepositoryImpl: TicketRepositoryImpl
    ): TicketRepository

    @Binds
    @Singleton
    abstract fun bindSyncQueueRepository(
        syncQueueRepositoryImpl: SyncQueueRepositoryImpl
    ): SyncQueueRepository
}