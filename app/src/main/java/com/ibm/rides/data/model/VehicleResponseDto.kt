package com.ibm.rides.data.model
import com.google.gson.annotations.SerializedName

data class VehicleResponseDto(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("uid")
    val uid: String?,

    @SerializedName("vin")
    val vin: String?,

    @SerializedName("make_and_model")
    val makeAndModel: String?,

    @SerializedName("color")
    val color: String?,

    @SerializedName("transmission")
    val transmission: String?,

    @SerializedName("drive_type")
    val driveType: String?,

    @SerializedName("fuel_type")
    val fuelType: String?,

    @SerializedName("car_type")
    val carType: String?,

    @SerializedName("car_options")
    val carOptions: List<String>?,

    @SerializedName("specs")
    val specs: List<String>?,

    @SerializedName("doors")
    val doors: Int?,

    @SerializedName("mileage")
    val mileage: Int?,

    @SerializedName("kilometrage")
    val kilometrage: Int?,

    @SerializedName("license_plate")
    val licensePlate: String?
)
