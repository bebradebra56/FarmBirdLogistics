package com.farmbirdfs.logjfeiowewg.opf.domain.model

import com.google.gson.annotations.SerializedName


private const val FARM_BIRD_LOGISTICS_A = "com.farmbirdfs.logistsrisc"
private const val FARM_BIRD_LOGISTICS_B = "farmbirdlogisitcs"
data class FarmBirdLogisticsParam (
    @SerializedName("af_id")
    val farmBirdLogisticsAfId: String,
    @SerializedName("bundle_id")
    val farmBirdLogisticsBundleId: String = FARM_BIRD_LOGISTICS_A,
    @SerializedName("os")
    val farmBirdLogisticsOs: String = "Android",
    @SerializedName("store_id")
    val farmBirdLogisticsStoreId: String = FARM_BIRD_LOGISTICS_A,
    @SerializedName("locale")
    val farmBirdLogisticsLocale: String,
    @SerializedName("push_token")
    val farmBirdLogisticsPushToken: String,
    @SerializedName("firebase_project_id")
    val farmBirdLogisticsFirebaseProjectId: String = FARM_BIRD_LOGISTICS_B,

    )