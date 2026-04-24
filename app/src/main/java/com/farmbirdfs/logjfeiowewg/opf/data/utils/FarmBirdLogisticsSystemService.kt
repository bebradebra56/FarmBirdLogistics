package com.farmbirdfs.logjfeiowewg.opf.data.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.farmbirdfs.logjfeiowewg.opf.presentation.app.FarmBirdLogisticsApplication
import java.util.Locale

class FarmBirdLogisticsSystemService(private val context: Context) {

    fun farmBirdLogisticsGetAppsflyerId(): String {
        val farmBirdLogisticsId = AppsFlyerLib.getInstance().getAppsFlyerUID(context).orEmpty()
        Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "AppsFlyer: AppsFlyer Id = $farmBirdLogisticsId")
        return farmBirdLogisticsId
    }

    fun farmBirdLogisticsGetLocale() : String {
        return  Locale.getDefault().language
    }

    fun farmBirdLogisticsIsOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?: return false

        val transports = listOf(
            NetworkCapabilities.TRANSPORT_CELLULAR,
            NetworkCapabilities.TRANSPORT_WIFI,
            NetworkCapabilities.TRANSPORT_ETHERNET,
            NetworkCapabilities.TRANSPORT_VPN
        )
        return transports.any(capabilities::hasTransport)
    }

}