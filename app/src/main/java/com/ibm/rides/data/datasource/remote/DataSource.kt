package com.ibm.rides.data.datasource.remote

interface DataSource<T> {

    suspend fun fetchRandomVehicles(size: Int): T
}