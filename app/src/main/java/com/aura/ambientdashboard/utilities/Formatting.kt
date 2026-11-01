package com.aura.ambientdashboard.utilities

import java.util.Locale

object Formatting {

    fun uptime(ms: Long): String {
        val totalMinutes = ms / 60_000
        val days = totalMinutes / (24 * 60)
        val hours = (totalMinutes % (24 * 60)) / 60
        val minutes = totalMinutes % 60
        return when {
            days > 0 -> "$days d $hours h $minutes m"
            hours > 0 -> "$hours h $minutes m"
            else -> "$minutes m"
        }
    }

    fun megabytes(bytes: Long): Long = bytes / (1024L * 1024L)

    fun gigabytes(bytes: Long): String =
        String.format(Locale.UK, "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))

    fun percent(part: Long, whole: Long): Int =
        if (whole <= 0) 0 else ((part * 100) / whole).toInt().coerceIn(0, 100)
}
