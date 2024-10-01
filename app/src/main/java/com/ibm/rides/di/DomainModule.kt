package com.ibm.rides.di

import com.ibm.rides.domain.mapper.VehicleResponseDtoMapper
import com.ibm.rides.domain.repository.VehicleRepository
import com.ibm.rides.domain.usecase.VehiclesUSeCaseImpl
import com.ibm.rides.domain.usecase.VehiclesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class DomainModule {

    @Provides
    fun provideVehicleUseCase(
        repository: VehicleRepository
    ): VehiclesUseCase {
        return VehiclesUSeCaseImpl(
            repository
        )
    }

    @Provides
    fun provideRemoteDtoMapper(): VehicleResponseDtoMapper {
        return VehicleResponseDtoMapper()
    }


}