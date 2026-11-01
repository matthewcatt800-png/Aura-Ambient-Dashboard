package com.aura.ambientdashboard.ui

import android.content.res.Configuration

object AccentPalette {

    val names = listOf("cyan", "blue", "purple", "lavender", "green", "amber")

    private val dark = mapOf(
        "cyan" to 0xFF4DD8E6.toInt(),
        "blue" to 0xFF5B8CFF.toInt(),
        "purple" to 0xFF9A86F0.toInt(),
        "lavender" to 0xFFC3B8F0.toInt(),
        "green" to 0xFF5BC58E.toInt(),
        "amber" to 0xFFE8B45C.toInt()
    )

    private val light = mapOf(
        "cyan" to 0xFF0E8FA0.toInt(),
        "blue" to 0xFF2C5CD6.toInt(),
        "purple" to 0xFF6C55C7.toInt(),
        "lavender" to 0xFF8E7CD8.toInt(),
        "green" to 0xFF2E8B57.toInt(),
        "amber" to 0xFFC47F17.toInt()
    )

    fun color(name: String, nightMode: Boolean): Int =
        (if (nightMode) dark else light)[name] ?: dark.getValue("cyan")

    fun isNightMode(configuration: Configuration): Boolean =
        (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}
