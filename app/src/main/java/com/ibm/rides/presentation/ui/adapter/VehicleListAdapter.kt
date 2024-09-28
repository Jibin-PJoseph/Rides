package com.ibm.rides.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ibm.rides.databinding.VehicleItemBinding
import com.ibm.rides.domain.model.Vehicle

class VehicleListAdapter(
    private val vehicleSelectListener: VehicleSelectListener
) : ListAdapter<Vehicle, VehicleViewHolder>(VehicleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = VehicleItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return VehicleViewHolder(binding, vehicleSelectListener)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = getItem(position)
        holder.bind(vehicle)
    }
}
