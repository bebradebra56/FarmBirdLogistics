package com.farmbirdfs.logjfeiowewg.opf.domain.model

import com.google.gson.annotations.SerializedName


data class FarmBirdLogisticsEntity (
    @SerializedName("ok")
    val farmBirdLogisticsOk: String,
    @SerializedName("url")
    val farmBirdLogisticsUrl: String,
    @SerializedName("expires")
    val farmBirdLogisticsExpires: Long,
)