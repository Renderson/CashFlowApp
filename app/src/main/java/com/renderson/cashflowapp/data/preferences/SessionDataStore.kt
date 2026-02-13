package com.renderson.cashflowapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.sessionDataStore by preferencesDataStore(name = "session")

private val LAST_ACTIVE_TIMESTAMP_KEY = longPreferencesKey("last_active_timestamp")

class SessionDataStore(private val context: Context) {

    suspend fun setLastActiveTimestamp(timestamp: Long) {
        context.sessionDataStore.edit { preferences ->
            preferences[LAST_ACTIVE_TIMESTAMP_KEY] = timestamp
        }
    }

    suspend fun getLastActiveTimestamp(): Long? {
        return context.sessionDataStore.data.first()[LAST_ACTIVE_TIMESTAMP_KEY]
    }
}
