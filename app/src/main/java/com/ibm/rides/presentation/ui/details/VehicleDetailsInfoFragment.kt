package com.ibm.rides.presentation.ui.details

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibm.rides.databinding.VehicleDetailsInfoFragmentBinding
import com.ibm.rides.domain.model.Vehicle

class VehicleDetailsInfoFragment :  Fragment() {
    private  var binding: VehicleDetailsInfoFragmentBinding? = null

    companion object {
        private const val ARG_VEHICLE = "vehicle"

        fun newInstance(vehicle: Vehicle): VehicleDetailsInfoFragment {
            val fragment = VehicleDetailsInfoFragment()
            val args = Bundle().apply {
                putParcelable(ARG_VEHICLE, vehicle)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = VehicleDetailsInfoFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            val vehicle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_VEHICLE, Vehicle::class.java)  // For API level 33+
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable(ARG_VEHICLE)  // For API levels below 33
            }

            vehicle?.let { vehicleData ->
                bindVehicleDetailsInfo(vehicleData) }
        }

    }

    private fun bindVehicleDetailsInfo(vehicle: Vehicle) {
        binding?.apply {
            textVin.text = vehicle.vin
            textMakeModel.text = vehicle.makeAndModel
            textColor.text = vehicle.color
            textCarType.text = vehicle.carType
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


}