package com.ibm.rides.presentation.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.ibm.rides.databinding.VehicleItemBinding
import com.ibm.rides.domain.model.Vehicle

 class VehicleViewHolder(
    private val binding: VehicleItemBinding,
    private val vehicleSelectListener: VehicleSelectListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(vehicle: Vehicle) {
        with(binding){
            textViewVehicleMakeAndModel.text = vehicle.makeAndModel
            textViewVehicleVin.text = vehicle.vin
            itemView.setOnClickListener{

                vehicleSelectListener.onVehicleSelected(vehicle)
            }
        }
    }
}
