package com.ibm.rides.di

import com.ibm.rides.domain.mapper.VehicleResponseDtoMapper
import com.ibm.rides.domain.repository.VehicleRepository
import com.ibm.rides.domain.usecase.CalculateEmissionsUseCase
import com.ibm.rides.domain.usecase.SortVehiclesUseCase
import com.ibm.rides.domain.usecase.ValidateCountUseCase
import com.ibm.rides.domain.usecase.VehiclesUSeCaseImpl
import com.ibm.rides.domain.usecase.VehiclesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
    @Singleton
    fun provideValidateCountUseCase(): ValidateCountUseCase {
        return ValidateCountUseCase()
    }

    @Provides
    @Singleton
    fun provideCalculateEmissionsUseCase(): CalculateEmissionsUseCase {
        return CalculateEmissionsUseCase()
    }

    @Provides
    @Singleton
    fun provideSortVehiclesUseCase(): SortVehiclesUseCase {
        return SortVehiclesUseCase()
    }

    @Provides
    fun provideRemoteDtoMapper(): VehicleResponseDtoMapper {
        return VehicleResponseDtoMapper()
    }


}