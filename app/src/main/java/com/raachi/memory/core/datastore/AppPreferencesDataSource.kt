package com.raachi.memory.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private const val APP_PREFERENCES_NAME = "app_preferences"

val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = APP_PREFERENCES_NAME
)

/**
 * Provides a single DataStore Preferences access point for app settings.
 */
class AppPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val preferences: Flow<Preferences> = context.appPreferencesDataStore.data

    suspend fun updatePreferences(transform: MutablePreferences.() -> Unit) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences.transform()
        }
    }
}
