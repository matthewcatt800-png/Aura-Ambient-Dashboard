package com.aura.ambientdashboard.utilities

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object ClockFormatter {

    fun time(dateTime: LocalDateTime, use24Hour: Boolean, showSeconds: Boolean): String {
        val pattern = when {
            use24Hour && showSeconds -> "HH:mm:ss"
            use24Hour -> "HH:mm"
            showSeconds -> "h:mm:ss a"
            else -> "h:mm a"
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.UK))
    }

    fun longDate(dateTime: LocalDateTime): String =
        dateTime.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.UK))

    fun shortDate(dateTime: LocalDateTime): String =
        dateTime.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK))

    fun greeting(dateTime: LocalDateTime): String = when (dateTime.hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        in 18..21 -> "Good evening"
        else -> "Good night"
    }
}
