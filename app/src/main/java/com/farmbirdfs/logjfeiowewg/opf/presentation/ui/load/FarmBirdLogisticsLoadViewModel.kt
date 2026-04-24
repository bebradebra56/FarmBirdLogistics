package com.farmbirdfs.logjfeiowewg.opf.presentation.ui.load

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmbirdfs.logjfeiowewg.opf.data.shar.FarmBirdLogisticsSharedPreference
import com.farmbirdfs.logjfeiowewg.opf.data.utils.FarmBirdLogisticsSystemService
import com.farmbirdfs.logjfeiowewg.opf.domain.usecases.FarmBirdLogisticsGetAllUseCase
import com.farmbirdfs.logjfeiowewg.opf.presentation.app.FarmBirdLogisticsAppsFlyerState
import com.farmbirdfs.logjfeiowewg.opf.presentation.app.FarmBirdLogisticsApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FarmBirdLogisticsLoadViewModel(
    private val farmBirdLogisticsGetAllUseCase: FarmBirdLogisticsGetAllUseCase,
    private val farmBirdLogisticsSharedPreference: FarmBirdLogisticsSharedPreference,
    private val farmBirdLogisticsSystemService: FarmBirdLogisticsSystemService
) : ViewModel() {

    private val _farmBirdLogisticsHomeScreenState: MutableStateFlow<FarmBirdLogisticsHomeScreenState> =
        MutableStateFlow(FarmBirdLogisticsHomeScreenState.FarmBirdLogisticsLoading)
    val farmBirdLogisticsHomeScreenState = _farmBirdLogisticsHomeScreenState.asStateFlow()

    private var farmBirdLogisticsRequestAlreadyConsumed = false


    init {
        viewModelScope.launch {
            farmBirdLogisticsBootstrap()
        }
    }

    private suspend fun farmBirdLogisticsBootstrap() {
        if (farmBirdLogisticsSystemService.farmBirdLogisticsIsOnline().not()) {
            farmBirdLogisticsEmitState(FarmBirdLogisticsHomeScreenState.FarmBirdLogisticsNotInternet)
            return
        }

        when (farmBirdLogisticsSharedPreference.farmBirdLogisticsAppState) {
            0 -> farmBirdLogisticsHandleFreshApp()
            1 -> farmBirdLogisticsHandleInitializedApp()
            2 -> farmBirdLogisticsEmitState(FarmBirdLogisticsHomeScreenState.FarmBirdLogisticsError)
        }
    }

    private suspend fun farmBirdLogisticsHandleFreshApp() {
        farmBirdLogisticsCollectConversion { conversionState ->
            when (conversionState) {
                FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsDefault -> Unit
                FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsError -> {
                    farmBirdLogisticsSharedPreference.farmBirdLogisticsAppState = 2
                    farmBirdLogisticsEmitState(FarmBirdLogisticsHomeScreenState.FarmBirdLogisticsError)
                    farmBirdLogisticsRequestAlreadyConsumed = true
                }
                is FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsSuccess -> {
                    farmBirdLogisticsTryFetch(conversionState.farmBirdLogisticsData)
                }
            }
        }
    }

    private suspend fun farmBirdLogisticsHandleInitializedApp() {
        val pushUrl = FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_FB_LI
        if (pushUrl != null) {
            farmBirdLogisticsEmitState(FarmBirdLogisticsHomeScreenState.FarmBirdLogisticsSuccess(pushUrl))
            return
        }

        if (farmBirdLogisticsIsStoredUrlValid()) {
            Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "Current time less then expired, use saved url")
            farmBirdLogisticsEmitSavedUrl()
            return
        }

        Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "Current time more then expired, repeat request")
        farmBirdLogisticsCollectConversion { conversionState ->
            when (conversionState) {
                FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsDefault -> Unit
                FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsError -> {
                    farmBirdLogisticsEmitSavedUrl()
                    farmBirdLogisticsRequestAlreadyConsumed = true
                }
                is FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsSuccess -> {
                    farmBirdLogisticsTryFetch(conversionState.farmBirdLogisticsData)
                }
            }
        }
    }

    private suspend fun farmBirdLogisticsCollectConversion(
        collector: suspend (FarmBirdLogisticsAppsFlyerState) -> Unit
    ) {
        FarmBirdLogisticsApplication.farmBirdLogisticsConversionFlow.collectLatest(collector)
    }

    private suspend fun farmBirdLogisticsTryFetch(conversion: MutableMap<String, Any>?) {
        if (farmBirdLogisticsRequestAlreadyConsumed) return
        farmBirdLogisticsRequestAlreadyConsumed = true
        farmBirdLogisticsGetData(conversion)
    }

    private fun farmBirdLogisticsIsStoredUrlValid(): Boolean {
        val nowUnixSec = System.currentTimeMillis() / 1000
        return nowUnixSec <= farmBirdLogisticsSharedPreference.farmBirdLogisticsExpired
    }

    private fun farmBirdLogisticsEmitSavedUrl() {
        farmBirdLogisticsEmitState(
            FarmBirdLogisticsHomeScreenState.FarmBirdLogisticsSuccess(
                farmBirdLogisticsSharedPreference.farmBirdLogisticsSavedUrl
            )
        )
    }

    private fun farmBirdLogisticsEmitState(state: FarmBirdLogisticsHomeScreenState) {
        _farmBirdLogisticsHomeScreenState.value = state
    }

    private suspend fun farmBirdLogisticsGetData(conversation: MutableMap<String, Any>?) {
        val farmBirdLogisticsData = farmBirdLogisticsGetAllUseCase(conversation)
        val isFirstLaunch = farmBirdLogisticsSharedPreference.farmBirdLogisticsAppState == 0

        if (farmBirdLogisticsData == null) {
            if (isFirstLaunch) {
                farmBirdLogisticsSharedPreference.farmBirdLogisticsAppState = 2
                farmBirdLogisticsEmitState(FarmBirdLogisticsHomeScreenState.FarmBirdLogisticsError)
            } else {
                farmBirdLogisticsEmitSavedUrl()
            }
            return
        }

        if (isFirstLaunch) {
            farmBirdLogisticsSharedPreference.farmBirdLogisticsAppState = 1
        }
        farmBirdLogisticsSharedPreference.apply {
            farmBirdLogisticsExpired = farmBirdLogisticsData.farmBirdLogisticsExpires
            farmBirdLogisticsSavedUrl = farmBirdLogisticsData.farmBirdLogisticsUrl
        }
        farmBirdLogisticsEmitState(
            FarmBirdLogisticsHomeScreenState.FarmBirdLogisticsSuccess(farmBirdLogisticsData.farmBirdLogisticsUrl)
        )
    }


    sealed class FarmBirdLogisticsHomeScreenState {
        data object FarmBirdLogisticsLoading : FarmBirdLogisticsHomeScreenState()
        data object FarmBirdLogisticsError : FarmBirdLogisticsHomeScreenState()
        data class FarmBirdLogisticsSuccess(val data: String) : FarmBirdLogisticsHomeScreenState()
        data object FarmBirdLogisticsNotInternet: FarmBirdLogisticsHomeScreenState()
    }
}