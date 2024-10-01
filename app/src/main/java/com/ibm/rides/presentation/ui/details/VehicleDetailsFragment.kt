package com.ibm.rides.presentation.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.ibm.rides.databinding.FragmentVehicleDetailsBinding
import com.ibm.rides.presentation.ui.adapter.VehicleDetailsPagerAdapter

class VehicleDetailsFragment :  Fragment() {
    private  var binding: FragmentVehicleDetailsBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVehicleDetailsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vehicleDetails = VehicleDetailsFragmentArgs.fromBundle(requireArguments()).vehicle

        binding?.let { binding ->
            val viewPager: ViewPager2 = binding.viewPager
            val tabLayout = binding.tabLayout

            val fragments = listOf(
                VehicleDetailsInfoFragment.newInstance(vehicleDetails),
                EstimatedCarbonEmissionInfoFragment.newInstance(vehicleDetails)
            )
            viewPager.adapter = VehicleDetailsPagerAdapter(this, fragments)

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Vehicle Details"
                    1 -> "Estimated Carbon Emission"
                    else -> null
                }
            }.attach()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}