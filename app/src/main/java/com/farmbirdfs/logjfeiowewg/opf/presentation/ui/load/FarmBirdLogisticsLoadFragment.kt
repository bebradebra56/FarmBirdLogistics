package com.farmbirdfs.logjfeiowewg.opf.presentation.ui.load

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.farmbirdfs.logjfeiowewg.MainActivity
import com.farmbirdfs.logjfeiowewg.R
import com.farmbirdfs.logjfeiowewg.databinding.FragmentLoadFarmBirdLogisticsBinding
import com.farmbirdfs.logjfeiowewg.opf.data.shar.FarmBirdLogisticsSharedPreference
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class FarmBirdLogisticsLoadFragment : Fragment(R.layout.fragment_load_farm_bird_logistics) {
    private lateinit var farmBirdLogisticsLoadBinding: FragmentLoadFarmBirdLogisticsBinding

    private val farmBirdLogisticsLoadViewModel by viewModel<FarmBirdLogisticsLoadViewModel>()

    private val farmBirdLogisticsSharedPreference by inject<FarmBirdLogisticsSharedPreference>()

    private var farmBirdLogisticsUrl = ""

    private val farmBirdLogisticsRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        farmBirdLogisticsSharedPreference.farmBirdLogisticsNotificationState = 2
        farmBirdLogisticsNavigateToSuccess(farmBirdLogisticsUrl)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        farmBirdLogisticsLoadBinding = FragmentLoadFarmBirdLogisticsBinding.bind(view)

        farmBirdLogisticsLoadBinding.farmBirdLogisticsGrandButton.setOnClickListener {
            val farmBirdLogisticsPermission = Manifest.permission.POST_NOTIFICATIONS
            farmBirdLogisticsRequestNotificationPermission.launch(farmBirdLogisticsPermission)
        }

        farmBirdLogisticsLoadBinding.farmBirdLogisticsSkipButton.setOnClickListener {
            farmBirdLogisticsSharedPreference.farmBirdLogisticsNotificationState = 1
            farmBirdLogisticsSharedPreference.farmBirdLogisticsNotificationRequest = (System.currentTimeMillis() / 1000) + 259200
            farmBirdLogisticsNavigateToSuccess(farmBirdLogisticsUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                farmBirdLogisticsLoadViewModel.farmBirdLogisticsHomeScreenState.collect {
                    when (it) {
                        is FarmBirdLogisticsLoadViewModel.FarmBirdLogisticsHomeScreenState.FarmBirdLogisticsLoading -> Unit
                        is FarmBirdLogisticsLoadViewModel.FarmBirdLogisticsHomeScreenState.FarmBirdLogisticsError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is FarmBirdLogisticsLoadViewModel.FarmBirdLogisticsHomeScreenState.FarmBirdLogisticsSuccess -> {
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                                farmBirdLogisticsNavigateToSuccess(it.data)
                            } else {
                                farmBirdLogisticsHandleNotificationStep(it.data)
                            }
                        }

                        FarmBirdLogisticsLoadViewModel.FarmBirdLogisticsHomeScreenState.FarmBirdLogisticsNotInternet -> {
                            farmBirdLogisticsLoadBinding.farmBirdLogisticsStateGroup.visibility = View.VISIBLE
                            farmBirdLogisticsLoadBinding.farmBirdLogisticsLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun farmBirdLogisticsHandleNotificationStep(url: String) {
        when (farmBirdLogisticsSharedPreference.farmBirdLogisticsNotificationState) {
            0 -> farmBirdLogisticsShowNotificationChoice(url)
            1 -> {
                val isDeferredRequestExpired = System.currentTimeMillis() / 1000 >
                    farmBirdLogisticsSharedPreference.farmBirdLogisticsNotificationRequest
                if (isDeferredRequestExpired) {
                    farmBirdLogisticsShowNotificationChoice(url)
                } else {
                    farmBirdLogisticsNavigateToSuccess(url)
                }
            }
            2 -> farmBirdLogisticsNavigateToSuccess(url)
        }
    }

    private fun farmBirdLogisticsShowNotificationChoice(url: String) {
        farmBirdLogisticsLoadBinding.farmBirdLogisticsNotiGroup.visibility = View.VISIBLE
        farmBirdLogisticsLoadBinding.farmBirdLogisticsLoadingGroup.visibility = View.GONE
        farmBirdLogisticsUrl = url
    }


    private fun farmBirdLogisticsNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_farmBirdLogisticsLoadFragment_to_farmBirdLogisticsV,
            bundleOf(FARM_BIRD_LOGISTICS_D to data)
        )
    }

    companion object {
        const val FARM_BIRD_LOGISTICS_D = "farmBirdLogisticsData"
    }
}