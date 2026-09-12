package com.vajra.launcher.data.config

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var theme: String
        get() = prefs.getString(KEY_THEME, THEME_DARK) ?: THEME_DARK
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var accentColor: String
        get() = prefs.getString(KEY_ACCENT, ACCENT_BLUE) ?: ACCENT_BLUE
        set(value) = prefs.edit().putString(KEY_ACCENT, value).apply()

    var telemetryIntervalMs: Long
        get() = prefs.getLong(KEY_TELEMETRY_INTERVAL, DEFAULT_TELEMETRY_INTERVAL)
        set(value) = prefs.edit().putLong(KEY_TELEMETRY_INTERVAL, value).apply()

    fun resetToDefaults() {
        prefs.edit()
            .putString(KEY_THEME, THEME_DARK)
            .putString(KEY_ACCENT, ACCENT_BLUE)
            .putLong(KEY_TELEMETRY_INTERVAL, DEFAULT_TELEMETRY_INTERVAL)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "vajra_settings"
        private const val KEY_THEME = "pref_theme"
        private const val KEY_ACCENT = "pref_accent"
        private const val KEY_TELEMETRY_INTERVAL = "pref_telemetry_interval"

        const val THEME_DARK = "dark"
        const val THEME_AMOLED = "amoled"
        const val THEME_LIGHT = "light"

        const val ACCENT_BLUE = "blue"
        const val ACCENT_CYAN = "cyan"

        const val DEFAULT_TELEMETRY_INTERVAL = 3000L
    }
}
