package com.motibuddy.app

import android.content.Context
import android.content.SharedPreferences

class BotPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bot_prefs", Context.MODE_PRIVATE)

    var username: String
        get() = prefs.getString("username", "Buddy") ?: "Buddy"
        set(value) = prefs.edit().putString("username", value).apply()

    var message: String
        get() = prefs.getString("message", "Stay motivated!") ?: "Stay motivated!"
        set(value) = prefs.edit().putString("message", value).apply()

    var imageUri: String?
        get() = prefs.getString("imageUri", null)
        set(value) = prefs.edit().putString("imageUri", value).apply()

    var themeColor: String
        get() = prefs.getString("theme_color", "#6750A4") ?: "#6750A4"
        set(value) = prefs.edit().putString("theme_color", value).apply()
}
