package com.farmbirdfs.logjfeiowewg.opf.data.shar

import android.content.Context
import androidx.core.content.edit

class FarmBirdLogisticsSharedPreference(context: Context) {
    private val farmBirdLogisticsPrefs = context.getSharedPreferences("farmBirdLogisticsSharedPrefsAb", Context.MODE_PRIVATE)

    var farmBirdLogisticsSavedUrl: String
        get() = farmBirdLogisticsReadString(FARM_BIRD_LOGISTICS_SAVED_URL)
        set(value) = farmBirdLogisticsWriteString(FARM_BIRD_LOGISTICS_SAVED_URL, value)

    var farmBirdLogisticsExpired : Long
        get() = farmBirdLogisticsReadLong(FARM_BIRD_LOGISTICS_EXPIRED)
        set(value) = farmBirdLogisticsWriteLong(FARM_BIRD_LOGISTICS_EXPIRED, value)

    var farmBirdLogisticsAppState: Int
        get() = farmBirdLogisticsReadInt(FARM_BIRD_LOGISTICS_APPLICATION_STATE)
        set(value) = farmBirdLogisticsWriteInt(FARM_BIRD_LOGISTICS_APPLICATION_STATE, value)

    var farmBirdLogisticsNotificationRequest: Long
        get() = farmBirdLogisticsReadLong(FARM_BIRD_LOGISTICS_NOTIFICAITON_REQUEST)
        set(value) = farmBirdLogisticsWriteLong(FARM_BIRD_LOGISTICS_NOTIFICAITON_REQUEST, value)


    var farmBirdLogisticsNotificationState:Int
        get() = farmBirdLogisticsReadInt(FARM_BIRD_LOGISTICS_NOTIFICATION_STATE)
        set(value) = farmBirdLogisticsWriteInt(FARM_BIRD_LOGISTICS_NOTIFICATION_STATE, value)

    private fun farmBirdLogisticsReadString(key: String): String {
        return farmBirdLogisticsPrefs.getString(key, "") ?: ""
    }

    private fun farmBirdLogisticsReadLong(key: String): Long {
        return farmBirdLogisticsPrefs.getLong(key, 0L)
    }

    private fun farmBirdLogisticsReadInt(key: String): Int {
        return farmBirdLogisticsPrefs.getInt(key, 0)
    }

    private fun farmBirdLogisticsWriteString(key: String, value: String) {
        farmBirdLogisticsPrefs.edit { putString(key, value) }
    }

    private fun farmBirdLogisticsWriteLong(key: String, value: Long) {
        farmBirdLogisticsPrefs.edit { putLong(key, value) }
    }

    private fun farmBirdLogisticsWriteInt(key: String, value: Int) {
        farmBirdLogisticsPrefs.edit { putInt(key, value) }
    }

    companion object {
        private const val FARM_BIRD_LOGISTICS_NOTIFICATION_STATE = "farmBirdLogisticsNotificationState"
        private const val FARM_BIRD_LOGISTICS_SAVED_URL = "farmBirdLogisticsSavedUrl"
        private const val FARM_BIRD_LOGISTICS_EXPIRED = "farmBirdLogisticsExpired"
        private const val FARM_BIRD_LOGISTICS_APPLICATION_STATE = "farmBirdLogisticsApplicationState"
        private const val FARM_BIRD_LOGISTICS_NOTIFICAITON_REQUEST = "farmBirdLogisticsNotificationRequest"
    }
}