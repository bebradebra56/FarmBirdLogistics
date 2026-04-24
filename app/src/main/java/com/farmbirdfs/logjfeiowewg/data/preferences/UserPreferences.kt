package com.farmbirdfs.logjfeiowewg.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "farm_bird_prefs")

class UserPreferences(private val context: Context) {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val USER_NAME = stringPreferencesKey("user_name")
        val FARM_NAME = stringPreferencesKey("farm_name")
        val USE_METRIC = booleanPreferencesKey("use_metric")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val TEMP_UNIT = stringPreferencesKey("temp_unit")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.ONBOARDING_DONE] ?: false }

    val userName: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.USER_NAME] ?: "" }

    val farmName: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.FARM_NAME] ?: "" }

    val useMetric: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.USE_METRIC] ?: true }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    val tempUnit: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.TEMP_UNIT] ?: "°C" }

    val weightUnit: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.WEIGHT_UNIT] ?: "kg" }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[Keys.USER_NAME] = name }
    }

    suspend fun setFarmName(name: String) {
        context.dataStore.edit { it[Keys.FARM_NAME] = name }
    }

    suspend fun setUseMetric(metric: Boolean) {
        context.dataStore.edit {
            it[Keys.USE_METRIC] = metric
            it[Keys.TEMP_UNIT] = if (metric) "°C" else "°F"
            it[Keys.WEIGHT_UNIT] = if (metric) "kg" else "lb"
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
}
