package com.farmbirdfs.logjfeiowewg.opf.data.utils

import android.util.Log
import com.farmbirdfs.logjfeiowewg.opf.presentation.app.FarmBirdLogisticsApplication
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class FarmBirdLogisticsPushToken {

    suspend fun farmBirdLogisticsGetToken(
        farmBirdLogisticsMaxAttempts: Int = 3,
        farmBirdLogisticsDelayMs: Long = 1500
    ): String {
        val regularAttempts = (farmBirdLogisticsMaxAttempts - 1).coerceAtLeast(0)
        for (attemptIndex in 0 until regularAttempts) {
            try {
                return FirebaseMessaging.getInstance().token.await()
            } catch (e: Exception) {
                Log.e(
                    FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG,
                    "Token error (attempt ${attemptIndex + 1}): ${e.message}"
                )
                delay(farmBirdLogisticsDelayMs)
            }
        }

        return farmBirdLogisticsGetFallbackToken()
    }

    private suspend fun farmBirdLogisticsGetFallbackToken(): String {
        return runCatching { FirebaseMessaging.getInstance().token.await() }
            .getOrElse { error ->
                Log.e(
                    FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG,
                    "Token error final: ${error.message}"
                )
                "null"
            }
    }

}