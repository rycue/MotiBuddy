package com.motibuddy.app.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ThemeColorManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val PRIMARY_COLOR_KEY = "primary_color"
    private val defaultColor = Color(0xFF6750A4)

    // Observable state for Composables (used by MotiBuddyTheme)
    private val _primaryColor = MutableStateFlow(defaultColor)
    val primaryColor: StateFlow<Color> = _primaryColor

    // Load once when app starts or context changes
    fun loadFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedColorInt = prefs.getInt(PRIMARY_COLOR_KEY, defaultColor.toArgb())
        _primaryColor.value = Color(savedColorInt)
    }

    // Preferred method: sets and updates reactive state
    fun setCustomPrimary(context: Context, color: Color) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(PRIMARY_COLOR_KEY, color.toArgb()).apply()

        CoroutineScope(Dispatchers.Main).launch {
            _primaryColor.emit(color)
        }
    }

    // Convenience method (non-reactive): sets but doesn't update StateFlow
    fun setPrimaryColor(context: Context, color: Color) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(PRIMARY_COLOR_KEY, color.toArgb()).apply()
    }

    fun getPrimaryColor(context: Context): Color {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedColorInt = prefs.getInt(PRIMARY_COLOR_KEY, defaultColor.toArgb())
        return Color(savedColorInt)
    }
}