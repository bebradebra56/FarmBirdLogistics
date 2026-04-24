package com.farmbirdfs.logjfeiowewg.opf.presentation.app

import android.app.Application
import android.util.Log
import android.view.WindowManager
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.farmbirdfs.logjfeiowewg.di.appModule
import com.farmbirdfs.logjfeiowewg.opf.presentation.di.farmBirdLogisticsModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


sealed interface FarmBirdLogisticsAppsFlyerState {
    data object FarmBirdLogisticsDefault : FarmBirdLogisticsAppsFlyerState
    data class FarmBirdLogisticsSuccess(val farmBirdLogisticsData: MutableMap<String, Any>?) :
        FarmBirdLogisticsAppsFlyerState

    data object FarmBirdLogisticsError : FarmBirdLogisticsAppsFlyerState
}

interface FarmBirdLogisticsAppsApi {
    @Headers("Content-Type: application/json")
    @GET(FARM_BIRD_LOGISTICS_LIN)
    fun farmBirdLogisticsGetClient(
        @Query("devkey") devkey: String,
        @Query("device_id") deviceId: String,
    ): Call<MutableMap<String, Any>?>
}

private const val FARM_BIRD_LOGISTICS_APP_DEV = "KmCPKcKrHmvMhV9gypuisT"
private const val FARM_BIRD_LOGISTICS_LIN = "com.farmbirdfs.logistsrisc"

class FarmBirdLogisticsApplication : Application() {

    private var farmBirdLogisticsIsResumed = false
    ///////
    private var farmBirdLogisticsConversionTimeoutJob: Job? = null
    private var farmBirdLogisticsDeepLinkData: MutableMap<String, Any>? = null

    override fun onCreate() {
        super.onCreate()

        val appsflyer = AppsFlyerLib.getInstance().also {
            farmBirdLogisticsSetDebufLogger(it)
            farmBirdLogisticsMinTimeBetween(it)
        }

        farmBirdLogisticsInitDeepLinkSubscription(appsflyer)
        farmBirdLogisticsInitConversionListener(appsflyer)

        appsflyer.start(this, FARM_BIRD_LOGISTICS_APP_DEV, object : AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "AppsFlyer started")
            }

            override fun onError(p0: Int, p1: String) {
                Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "AppsFlyer start error: $p0 - $p1")
            }
        })
        farmBirdLogisticsStartConversionTimeout()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@FarmBirdLogisticsApplication)
            modules(
                listOf(
                    farmBirdLogisticsModule, appModule
                )
            )
        }
    }

    private fun farmBirdLogisticsInitDeepLinkSubscription(appsflyer: AppsFlyerLib) {
        appsflyer.subscribeForDeepLink(object : DeepLinkListener {
            override fun onDeepLinking(result: DeepLinkResult) {
                when (result.status) {
                    DeepLinkResult.Status.FOUND -> {
                        farmBirdLogisticsExtractDeepMap(result.deepLink)
                        Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "onDeepLinking found: ${result.deepLink}")
                    }
                    DeepLinkResult.Status.NOT_FOUND -> {
                        Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "onDeepLinking not found: ${result.deepLink}")
                    }
                    DeepLinkResult.Status.ERROR -> {
                        Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "onDeepLinking error: ${result.error}")
                    }
                }
            }
        })
    }

    private fun farmBirdLogisticsInitConversionListener(appsflyer: AppsFlyerLib) {
        appsflyer.init(
            FARM_BIRD_LOGISTICS_APP_DEV,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                    farmBirdLogisticsConversionTimeoutJob?.cancel()
                    Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "onConversionDataSuccess: $data")

                    val afStatus = data?.get("af_status")?.toString().orEmpty()
                    if (afStatus != "Organic") {
                        farmBirdLogisticsResume(FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsSuccess(data))
                        return
                    }
                    farmBirdLogisticsRequestDelayedInstallData()
                }

                override fun onConversionDataFail(error: String?) {
                    farmBirdLogisticsConversionTimeoutJob?.cancel()
                    Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "onConversionDataFail: $error")
                    farmBirdLogisticsResume(FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsError)
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "onAppOpenAttribution")
                }

                override fun onAttributionFailure(p0: String?) {
                    Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "onAttributionFailure: $p0")
                }
            },
            this
        )
    }

    private fun farmBirdLogisticsRequestDelayedInstallData() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                delay(5000)
                val response = farmBirdLogisticsGetApi(
                    "https://gcdsdk.appsflyer.com/install_data/v4.0/",
                    null
                ).farmBirdLogisticsGetClient(
                    devkey = FARM_BIRD_LOGISTICS_APP_DEV,
                    deviceId = farmBirdLogisticsGetAppsflyerId()
                ).awaitResponse()
                response.body()
            }.onSuccess { payload ->
                Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "After 5s: $payload")
                val status = payload?.get("af_status")
                if (status == "Organic" || status == null) {
                    farmBirdLogisticsResume(FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsError)
                } else {
                    farmBirdLogisticsResume(FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsSuccess(payload))
                }
            }.onFailure { error ->
                Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "Error: ${error.message}")
                farmBirdLogisticsResume(FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsError)
            }
        }
    }

    private fun farmBirdLogisticsExtractDeepMap(dl: DeepLink) {
        val map = linkedMapOf<String, Any>().apply {
            dl.deepLinkValue?.let { put("deep_link_value", it) }
            dl.mediaSource?.let { put("media_source", it) }
            dl.campaign?.let { put("campaign", it) }
            dl.campaignId?.let { put("campaign_id", it) }
            dl.afSub1?.let { put("af_sub1", it) }
            dl.afSub2?.let { put("af_sub2", it) }
            dl.afSub3?.let { put("af_sub3", it) }
            dl.afSub4?.let { put("af_sub4", it) }
            dl.afSub5?.let { put("af_sub5", it) }
            dl.matchType?.let { put("match_type", it) }
            dl.clickHttpReferrer?.let { put("click_http_referrer", it) }
            dl.getStringValue("timestamp")?.let { put("timestamp", it) }
            dl.isDeferred?.let { put("is_deferred", it) }
        }
        for (i in 1..10) {
            val key = "deep_link_sub$i"
            dl.getStringValue(key)?.takeIf { !map.containsKey(key) }?.let { map[key] = it }
        }
        Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "Extracted DeepLink data: $map")
        farmBirdLogisticsDeepLinkData = map
    }
    /////////////////

    private fun farmBirdLogisticsStartConversionTimeout() {
        farmBirdLogisticsConversionTimeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(30000)
            if (farmBirdLogisticsIsResumed) return@launch
            Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "TIMEOUT: No conversion data received in 30s")
            farmBirdLogisticsResume(FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsError)
        }
    }

    private fun farmBirdLogisticsResume(state: FarmBirdLogisticsAppsFlyerState) {
        farmBirdLogisticsConversionTimeoutJob?.cancel()
        if (farmBirdLogisticsIsResumed) return
        farmBirdLogisticsIsResumed = true

        farmBirdLogisticsConversionFlow.value = when (state) {
            is FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsSuccess -> {
                val convData = state.farmBirdLogisticsData ?: mutableMapOf()
                val deepData = farmBirdLogisticsDeepLinkData ?: mutableMapOf()
                FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsSuccess(
                    mutableMapOf<String, Any>().apply {
                        putAll(convData)
                        deepData.forEach { (key, value) -> if (!containsKey(key)) put(key, value) }
                    }
                )
            }
            else -> state
        }
    }

    private fun farmBirdLogisticsGetAppsflyerId(): String {
        val appsflyrid = AppsFlyerLib.getInstance().getAppsFlyerUID(this) ?: ""
        Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "AppsFlyer: AppsFlyer Id = $appsflyrid")
        return appsflyrid
    }

    private fun farmBirdLogisticsSetDebufLogger(appsflyer: AppsFlyerLib) {
        appsflyer.setDebugLog(true)
    }

    private fun farmBirdLogisticsMinTimeBetween(appsflyer: AppsFlyerLib) {
        appsflyer.setMinTimeBetweenSessions(0)
    }

    private fun farmBirdLogisticsGetApi(url: String, client: OkHttpClient?): FarmBirdLogisticsAppsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }

    companion object {
        var farmBirdLogisticsInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        val farmBirdLogisticsConversionFlow: MutableStateFlow<FarmBirdLogisticsAppsFlyerState> = MutableStateFlow(
            FarmBirdLogisticsAppsFlyerState.FarmBirdLogisticsDefault
        )
        var FARM_BIRD_LOGISTICS_FB_LI: String? = null
        const val FARM_BIRD_LOGISTICS_MAIN_TAG = "FarmBirdLogisticsMainTag"
    }
}