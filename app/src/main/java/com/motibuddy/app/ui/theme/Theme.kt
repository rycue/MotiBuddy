package com.motibuddy.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MotiBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Load color from SharedPreferences on first composition
    LaunchedEffect(Unit) {
        ThemeColorManager.loadFromPrefs(context)
    }

    // Observe live primary color from ThemeColorManager
    val primaryColor by ThemeColorManager.primaryColor.collectAsState()

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            dynamicLightColorScheme(context).copy(primary = primaryColor)
        darkTheme -> darkColorScheme(primary = primaryColor)
        else -> lightColorScheme(primary = primaryColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
