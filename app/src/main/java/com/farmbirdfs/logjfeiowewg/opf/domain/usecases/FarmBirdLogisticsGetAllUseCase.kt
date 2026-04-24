package com.farmbirdfs.logjfeiowewg.opf.domain.usecases

import android.util.Log
import com.farmbirdfs.logjfeiowewg.opf.data.repo.FarmBirdLogisticsRepository
import com.farmbirdfs.logjfeiowewg.opf.data.utils.FarmBirdLogisticsPushToken
import com.farmbirdfs.logjfeiowewg.opf.data.utils.FarmBirdLogisticsSystemService
import com.farmbirdfs.logjfeiowewg.opf.domain.model.FarmBirdLogisticsEntity
import com.farmbirdfs.logjfeiowewg.opf.domain.model.FarmBirdLogisticsParam
import com.farmbirdfs.logjfeiowewg.opf.presentation.app.FarmBirdLogisticsApplication

class FarmBirdLogisticsGetAllUseCase(
    private val farmBirdLogisticsRepository: FarmBirdLogisticsRepository,
    private val farmBirdLogisticsSystemService: FarmBirdLogisticsSystemService,
    private val farmBirdLogisticsPushToken: FarmBirdLogisticsPushToken,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?) : FarmBirdLogisticsEntity?{
        val farmBirdLogisticsRequestParams = FarmBirdLogisticsParam(
            farmBirdLogisticsLocale = farmBirdLogisticsSystemService.farmBirdLogisticsGetLocale(),
            farmBirdLogisticsPushToken = farmBirdLogisticsPushToken.farmBirdLogisticsGetToken(),
            farmBirdLogisticsAfId = farmBirdLogisticsSystemService.farmBirdLogisticsGetAppsflyerId()
        )
        Log.d(
            FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG,
            "Params for request: $farmBirdLogisticsRequestParams"
        )
        return farmBirdLogisticsRepository.farmBirdLogisticsGetClient(
            farmBirdLogisticsRequestParams,
            conversion
        )
    }



}