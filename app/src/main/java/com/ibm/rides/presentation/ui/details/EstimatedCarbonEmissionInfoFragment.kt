package com.ibm.rides.presentation.ui.details

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ibm.rides.databinding.EstimatedCarbonEmissionInfoFragmentBinding
import com.ibm.rides.domain.model.Vehicle
import com.ibm.rides.presentation.ui.state.VehicleUiState
import com.ibm.rides.presentation.viewmodel.VehicleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EstimatedCarbonEmissionInfoFragment :  Fragment() {
    private  var _binding: EstimatedCarbonEmissionInfoFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VehicleViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EstimatedCarbonEmissionInfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            val vehicleInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_VEHICLE, Vehicle::class.java)  // For API level 33+
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable(ARG_VEHICLE)  // For API levels below 33
            }

            vehicleInfo?.let { vehicle ->
                val kilometrage = vehicle.kilometrage
                if (kilometrage != null) {
                    viewModel.calculateAndSetEmissions(kilometrage)
                } else {
                    binding.textViewEstimatedCarbonEmissions.text = "Kilometrage data is unavailable"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    handleUiState(uiState)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleUiState(uiState: VehicleUiState) {
        when (uiState) {
            is VehicleUiState.CarbonEmissionsSuccess -> {
                binding.textViewEstimatedCarbonEmissions.text =
                    "Estimated Carbon Emissions: ${uiState.emissions} units"
            }
            else -> {
            }
        }
    }
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
}