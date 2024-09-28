package com.ibm.rides.presentation.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.ibm.rides.domain.model.Vehicle
class VehicleDiffCallback : DiffUtil.ItemCallback<Vehicle>() {

    override fun areItemsTheSame(
        oldItem: Vehicle,
        newItem: Vehicle
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Vehicle,
        newItem: Vehicle): Boolean {
        return oldItem == newItem
    }
}