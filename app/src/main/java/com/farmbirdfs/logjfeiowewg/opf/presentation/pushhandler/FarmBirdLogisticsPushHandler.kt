package com.farmbirdfs.logjfeiowewg.opf.presentation.pushhandler

import android.os.Bundle
import android.util.Log
import com.farmbirdfs.logjfeiowewg.opf.presentation.app.FarmBirdLogisticsApplication

class FarmBirdLogisticsPushHandler {
    fun farmBirdLogisticsHandlePush(extras: Bundle?) {
        Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "Extras from Push = ${extras?.keySet()}")
        if (extras == null) {
            Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "Push data no!")
            return
        }

        val map = farmBirdLogisticsBundleToMap(extras)
        Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "Map from Push = $map")
        map["url"]?.let { url ->
            FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_FB_LI = url
            Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "UrlFromActivity = $map")
        }
    }

    private fun farmBirdLogisticsBundleToMap(extras: Bundle): Map<String, String?> {
        return extras.keySet().associateWith(extras::getString)
    }

}