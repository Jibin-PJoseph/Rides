package com.ibm.rides.presentation.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import com.ibm.rides.presentation.ui.state.VehicleUiState
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

        setupObservers()

        binding?.buttonFetchVehicles?.setOnClickListener {
            val countStr = binding?.textInputVehicleCount?.text.toString()
            val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            viewModel.validateAndFetchVehicles(countStr)
        }

        binding?.buttonSortByCarType?.setOnClickListener {
            viewModel.sortVehiclesByCarType()
        }
    }

    private fun setupObservers(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    handleUiState(uiState)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventState.collect { event ->
                    event?.getContentIfNotHandled()?.let { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun handleUiState(uiState: VehicleUiState) {
        when (uiState) {
            is VehicleUiState.IdleState -> {
                shouldShowProgress(false)
                binding?.buttonFetchVehicles?.isEnabled = true
            }
            is VehicleUiState.Loading -> {
                shouldShowProgress(true)
                binding?.buttonFetchVehicles?.isEnabled = false
            }
            is VehicleUiState.Success -> {
                shouldShowProgress(false)
                binding?.buttonFetchVehicles?.isEnabled = true
                vehicleListAdapter.submitList(uiState.vehicles) {
                    binding?.recyclerViewVehicleList?.scrollToPosition(0)
                }
            }
            is VehicleUiState.ValidationError -> {
                shouldShowProgress(false)
                binding?.buttonFetchVehicles?.isEnabled = true
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
            }
            is VehicleUiState.Error -> {
                shouldShowProgress(false)
                binding?.buttonFetchVehicles?.isEnabled = true
                Toast.makeText(context, uiState.message, Toast.LENGTH_LONG).show()
                uiState.vehicles?.let { vehicles ->
                    vehicleListAdapter.submitList(vehicles) {
                        binding?.recyclerViewVehicleList?.scrollToPosition(0)
                    }
                }
            }
            else -> {
                shouldShowProgress(false)
                binding?.buttonFetchVehicles?.isEnabled = true
            }
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
