package com.aura.ambientdashboard.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class AuraPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Appearance

    var themeMode: String
        get() = prefs.getString(KEY_THEME, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var accent: String
        get() = prefs.getString(KEY_ACCENT, "cyan") ?: "cyan"
        set(value) = prefs.edit().putString(KEY_ACCENT, value).apply()

    var cardAlpha: Float
        get() = prefs.getFloat(KEY_CARD_ALPHA, 0.85f)
        set(value) = prefs.edit().putFloat(KEY_CARD_ALPHA, value).apply()

    var animationIntensity: Float
        get() = prefs.getFloat(KEY_ANIMATION, 1f)
        set(value) = prefs.edit().putFloat(KEY_ANIMATION, value).apply()

    var reduceAnimations: Boolean
        get() = prefs.getBoolean(KEY_REDUCE_ANIMATIONS, false)
        set(value) = prefs.edit().putBoolean(KEY_REDUCE_ANIMATIONS, value).apply()

    // Clock

    var is24Hour: Boolean
        get() = prefs.getBoolean(KEY_HOUR_24, true)
        set(value) = prefs.edit().putBoolean(KEY_HOUR_24, value).apply()

    var showSeconds: Boolean
        get() = prefs.getBoolean(KEY_SHOW_SECONDS, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_SECONDS, value).apply()

    var clockStyle: String
        get() = prefs.getString(KEY_CLOCK_STYLE, "classic") ?: "classic"
        set(value) = prefs.edit().putString(KEY_CLOCK_STYLE, value).apply()

    var clockSizeSp: Int
        get() = prefs.getInt(KEY_CLOCK_SIZE, 64)
        set(value) = prefs.edit().putInt(KEY_CLOCK_SIZE, value).apply()

    // Ambient

    var ambientBackground: String
        get() = prefs.getString(KEY_AMBIENT_BG, "midnight") ?: "midnight"
        set(value) = prefs.edit().putString(KEY_AMBIENT_BG, value).apply()

    var ambientClockPosition: String
        get() = prefs.getString(KEY_AMBIENT_POS, "center") ?: "center"
        set(value) = prefs.edit().putString(KEY_AMBIENT_POS, value).apply()

    var ambientParticles: Boolean
        get() = prefs.getBoolean(KEY_AMBIENT_PARTICLES, true)
        set(value) = prefs.edit().putBoolean(KEY_AMBIENT_PARTICLES, value).apply()

    var ambientParticleIntensity: Float
        get() = prefs.getFloat(KEY_AMBIENT_PARTICLE_INTENSITY, 0.6f)
        set(value) = prefs.edit().putFloat(KEY_AMBIENT_PARTICLE_INTENSITY, value).apply()

    var ambientGlow: Float
        get() = prefs.getFloat(KEY_AMBIENT_GLOW, 0.6f)
        set(value) = prefs.edit().putFloat(KEY_AMBIENT_GLOW, value).apply()

    var ambientSpeed: Float
        get() = prefs.getFloat(KEY_AMBIENT_SPEED, 0.5f)
        set(value) = prefs.edit().putFloat(KEY_AMBIENT_SPEED, value).apply()

    var ambientShowInfo: Boolean
        get() = prefs.getBoolean(KEY_AMBIENT_INFO, true)
        set(value) = prefs.edit().putBoolean(KEY_AMBIENT_INFO, value).apply()

    // Monitoring

    var monitoringPaused: Boolean
        get() = prefs.getBoolean(KEY_MONITOR_PAUSED, false)
        set(value) = prefs.edit().putBoolean(KEY_MONITOR_PAUSED, value).apply()

    var monitoringIntervalSec: Int
        get() = prefs.getInt(KEY_MONITOR_INTERVAL, 2)
        set(value) = prefs.edit().putInt(KEY_MONITOR_INTERVAL, value).apply()

    // Weather

    var weatherCity: String
        get() = prefs.getString(KEY_WEATHER_CITY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEATHER_CITY, value).apply()

    var weatherUseLocation: Boolean
        get() = prefs.getBoolean(KEY_WEATHER_LOCATION, false)
        set(value) = prefs.edit().putBoolean(KEY_WEATHER_LOCATION, value).apply()

    // Navigation

    var selectedTab: Int
        get() = prefs.getInt(KEY_SELECTED_TAB, 0)
        set(value) = prefs.edit().putInt(KEY_SELECTED_TAB, value).apply()

    // Dashboard layout

    fun widgetState(): WidgetState {
        val raw = prefs.getString(KEY_WIDGET_STATE, null) ?: return WidgetState.default()
        return WidgetState.fromJson(raw)
    }

    fun saveWidgetState(state: WidgetState) {
        prefs.edit().putString(KEY_WIDGET_STATE, state.toJson()).apply()
    }

    fun savedLayoutNames(): List<String> {
        val root = JSONObject(prefs.getString(KEY_SAVED_LAYOUTS, "{}") ?: "{}")
        return root.keys().asSequence().sorted().toList()
    }

    fun saveLayout(name: String) {
        val root = JSONObject(prefs.getString(KEY_SAVED_LAYOUTS, "{}") ?: "{}")
        root.put(name, widgetState().toJson())
        prefs.edit().putString(KEY_SAVED_LAYOUTS, root.toString()).apply()
    }

    fun loadLayout(name: String): Boolean {
        val root = JSONObject(prefs.getString(KEY_SAVED_LAYOUTS, "{}") ?: "{}")
        val raw = root.optString(name, "")
        if (raw.isEmpty()) return false
        prefs.edit().putString(KEY_WIDGET_STATE, raw).apply()
        return true
    }

    fun resetDashboard() {
        prefs.edit().putString(KEY_WIDGET_STATE, null).apply()
    }

    private companion object {
        const val PREFS_NAME = "aura_prefs"
        const val KEY_THEME = "theme_mode"
        const val KEY_ACCENT = "accent"
        const val KEY_CARD_ALPHA = "card_alpha"
        const val KEY_ANIMATION = "animation_intensity"
        const val KEY_REDUCE_ANIMATIONS = "reduce_animations"
        const val KEY_HOUR_24 = "hour_24"
        const val KEY_SHOW_SECONDS = "show_seconds"
        const val KEY_CLOCK_STYLE = "clock_style"
        const val KEY_CLOCK_SIZE = "clock_size"
        const val KEY_AMBIENT_BG = "ambient_bg"
        const val KEY_AMBIENT_POS = "ambient_pos"
        const val KEY_AMBIENT_PARTICLES = "ambient_particles"
        const val KEY_AMBIENT_PARTICLE_INTENSITY = "ambient_particle_intensity"
        const val KEY_AMBIENT_GLOW = "ambient_glow"
        const val KEY_AMBIENT_SPEED = "ambient_speed"
        const val KEY_AMBIENT_INFO = "ambient_info"
        const val KEY_MONITOR_PAUSED = "monitor_paused"
        const val KEY_MONITOR_INTERVAL = "monitor_interval"
        const val KEY_WEATHER_CITY = "weather_city"
        const val KEY_WEATHER_LOCATION = "weather_use_location"
        const val KEY_SELECTED_TAB = "selected_tab"
        const val KEY_WIDGET_STATE = "widget_state"
        const val KEY_SAVED_LAYOUTS = "saved_layouts"
    }
}
