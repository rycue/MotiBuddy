// ThemePrefs.kt
package com.motibuddy.app

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_prefs")

object ThemeKeys {
    val PrimaryColor = stringPreferencesKey("primary_color")
    val SecondaryColor = stringPreferencesKey("secondary_color")
}

class ThemePrefs(private val context: Context) {

    suspend fun saveColors(primary: String, secondary: String) {
        context.dataStore.edit { prefs ->
            prefs[ThemeKeys.PrimaryColor] = primary
            prefs[ThemeKeys.SecondaryColor] = secondary
        }
    }

    suspend fun getColors(): Pair<String, String> {
        val prefs = context.dataStore.data.first()
        return Pair(
            prefs[ThemeKeys.PrimaryColor] ?: "#6750A4",
            prefs[ThemeKeys.SecondaryColor] ?: "#625B71"
        )
    }

    suspend fun savePrimaryColor(hex: String) {
        context.dataStore.edit { prefs ->
            prefs[ThemeKeys.PrimaryColor] = hex
        }
    }

    val primaryColorFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[ThemeKeys.PrimaryColor] ?: "#6750A4" }
}
