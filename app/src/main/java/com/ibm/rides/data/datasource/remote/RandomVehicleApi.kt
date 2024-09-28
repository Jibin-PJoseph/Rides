package com.ibm.rides.data.datasource.remote

import com.ibm.rides.data.model.VehicleResponseDto
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Singleton

@Singleton
interface RandomVehicleApi {

    @GET("random_vehicle")
    suspend fun getRandomVehicle(@Query("size") size: Int): List<VehicleResponseDto>
}