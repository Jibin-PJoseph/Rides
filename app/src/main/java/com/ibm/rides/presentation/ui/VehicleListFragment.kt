package com.ibm.rides.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ibm.rides.databinding.FragmentVehicleListBinding
import com.ibm.rides.domain.model.Vehicle
import com.ibm.rides.presentation.ui.adapter.VehicleListAdapter
import com.ibm.rides.presentation.ui.adapter.VehicleSelectListener
import com.ibm.rides.presentation.viewmodel.State
import com.ibm.rides.presentation.viewmodel.VehicleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VehicleListFragment : Fragment(), VehicleSelectListener {
    private var binding: FragmentVehicleListBinding? = null
    private val vehicleListAdapter by lazy(LazyThreadSafetyMode.NONE) {
        VehicleListAdapter(this)
    }
    private val viewModel: VehicleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVehicleListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.recyclerViewVehicleList?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = vehicleListAdapter
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect { state ->
                        updateState(state)
                    }
                }

                launch {
                    viewModel.validationResult.collect { result ->
                        result?.let {
                            if (!it.isValid) {
                                Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        binding?.buttonFetchVehicles?.setOnClickListener {
            val countStr = binding?.textInputVehicleCount?.text.toString()

            viewModel.validateAndFetchVehicles(countStr)
        }

        binding?.buttonSortByCarType?.setOnClickListener {
            viewModel.sortVehiclesByCarType()
        }

    }

    private fun updateState(state: State) {
        when (state) {
            is State.VehicleSuccess -> {
                shouldShowProgress(isVisible = false)
                vehicleListAdapter.submitList(state.vehicles) {
                    binding?.recyclerViewVehicleList?.scrollToPosition(0)
                }
            }

            is State.VehiclesError -> {
                shouldShowProgress(isVisible = false)
                Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
            }

            State.Loading -> shouldShowProgress(isVisible = true)
        }
    }

    private fun shouldShowProgress(isVisible: Boolean) {
        binding?.progress?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onVehicleSelected(vehicle: Vehicle) {

        val action = VehicleListFragmentDirections.actionToVehicleDetailsFragment(vehicle)
        findNavController().navigate(action)

    }

}