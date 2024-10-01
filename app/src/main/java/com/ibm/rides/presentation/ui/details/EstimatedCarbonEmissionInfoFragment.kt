package com.ibm.rides.presentation.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibm.rides.databinding.EstimatedCarbonEmissionInfoFragmentBinding
import com.ibm.rides.domain.model.Vehicle

class EstimatedCarbonEmissionInfoFragment :  Fragment() {
    private  var binding: EstimatedCarbonEmissionInfoFragmentBinding? = null
    companion object {
        private const val ARG_VEHICLE = "vehicle"

        fun newInstance(vehicle: Vehicle): EstimatedCarbonEmissionInfoFragment {
            val fragment = EstimatedCarbonEmissionInfoFragment()
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
        binding = EstimatedCarbonEmissionInfoFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


}