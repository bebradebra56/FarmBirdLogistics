package com.farmbirdfs.logjfeiowewg.opf.data.repo

import android.util.Log
import com.farmbirdfs.logjfeiowewg.opf.domain.model.FarmBirdLogisticsEntity
import com.farmbirdfs.logjfeiowewg.opf.domain.model.FarmBirdLogisticsParam
import com.farmbirdfs.logjfeiowewg.opf.presentation.app.FarmBirdLogisticsApplication.Companion.FARM_BIRD_LOGISTICS_MAIN_TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FarmBirdLogisticsApi {
    @Headers("Content-Type: application/json")
    @POST("config.php")
    fun farmBirdLogisticsGetClient(
        @Body jsonString: JsonObject,
    ): Call<FarmBirdLogisticsEntity>
}


private const val FARM_BIRD_LOGISTICS_MAIN = "https://farmbirdlogistics.com/"
class FarmBirdLogisticsRepository {

    suspend fun farmBirdLogisticsGetClient(
        farmBirdLogisticsParam: FarmBirdLogisticsParam,
        farmBirdLogisticsConversion: MutableMap<String, Any>?
    ): FarmBirdLogisticsEntity? {
        val gson = Gson()
        val farmBirdLogisticsJsonObject = farmBirdLogisticsBuildPayload(
            gson = gson,
            farmBirdLogisticsParam = farmBirdLogisticsParam,
            farmBirdLogisticsConversion = farmBirdLogisticsConversion
        )
        Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "Retrofit: Json: $farmBirdLogisticsJsonObject")

        return runCatching {
            farmBirdLogisticsRequestEntity(farmBirdLogisticsJsonObject)
        }.onFailure { error ->
            Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "Retrofit: Get request failed")
            Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "Retrofit: ${error.message}")
        }.getOrNull()
    }

    private suspend fun farmBirdLogisticsRequestEntity(
        farmBirdLogisticsJsonObject: JsonObject
    ): FarmBirdLogisticsEntity? {
        val request = farmBirdLogisticsGetApi(FARM_BIRD_LOGISTICS_MAIN, null).farmBirdLogisticsGetClient(
            jsonString = farmBirdLogisticsJsonObject,
        )
        Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "Retrofit: request: ${request.request().url}")

        val response = request.awaitResponse()
        val responseCode = response.code()
        Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "Retrofit: Result code: $responseCode")

        return when (responseCode) {
            200 -> {
                Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "Retrofit: Get request success")
                Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "Retrofit: Code = $responseCode")
                Log.d(FARM_BIRD_LOGISTICS_MAIN_TAG, "Retrofit: ${response.body()}")
                response.body()
            }
            else -> null
        }
    }

    private fun farmBirdLogisticsBuildPayload(
        gson: Gson,
        farmBirdLogisticsParam: FarmBirdLogisticsParam,
        farmBirdLogisticsConversion: MutableMap<String, Any>?
    ): JsonObject {
        return gson.toJsonTree(farmBirdLogisticsParam).asJsonObject.also { root ->
            farmBirdLogisticsConversion
                ?.asSequence()
                ?.map { (key, value) -> key to gson.toJsonTree(value) as JsonElement }
                ?.forEach { (key, element) -> root.add(key, element) }
        }
    }

    private fun farmBirdLogisticsGetApi(url: String, client: OkHttpClient?) : FarmBirdLogisticsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }


}
