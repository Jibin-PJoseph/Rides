package com.ibm.rides.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Vehicle(
    val id: Int?,
    val uid: String?,
    val vin: String?,
    val makeAndModel: String?,
    val color: String?,
    val transmission: String?,
    val driveType: String?,
    val fuelType: String?,
    val carType: String?,
    val carOptions: List<String>?,
    val specs: List<String>?,
    val doors: Int?,
    val mileage: Int?,
    val kilometrage: Int?,
    val licensePlate: String?
) : Parcelable
