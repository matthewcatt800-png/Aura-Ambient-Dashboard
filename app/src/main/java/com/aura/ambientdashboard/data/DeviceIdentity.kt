package com.aura.ambientdashboard.data

import android.os.Build
import java.io.File
import java.util.Locale

object DeviceIdentity {

    val manufacturer: String
        get() = Build.MANUFACTURER.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.UK) else it.toString()
        }

    val model: String get() = Build.MODEL

    val androidVersion: String
        get() = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"

    val buildDisplay: String get() = Build.DISPLAY.ifBlank { "Unavailable" }

    val fingerprint: String get() = Build.FINGERPRINT

    val board: String get() = Build.BOARD.ifBlank { "Unavailable" }

    val kernelVersion: String
        get() = System.getProperty("os.version") ?: readFirstLine("/proc/version") ?: "Unavailable"

    fun cpuCoreCount(): Int = Runtime.getRuntime().availableProcessors()

    fun cpuModel(): String = readHardwareLine() ?: Build.HARDWARE.takeIf { it.isNotBlank() } ?: "Unavailable"

    fun cpuMaxFrequencyMhz(): Int? =
        readFirstLine("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            ?.trim()?.toIntOrNull()?.let { it / 1000 }

    private fun readFirstLine(path: String): String? = try {
        File(path).bufferedReader().useLines { lines -> lines.firstOrNull()?.trim() }
    } catch (e: Exception) {
        null
    }

    private fun readHardwareLine(): String? = try {
        File("/proc/cpuinfo").bufferedReader().useLines { lines ->
            lines.firstOrNull { it.startsWith("Hardware", ignoreCase = true) }
                ?.substringAfter(':')?.trim()?.takeIf { it.isNotEmpty() }
        }
    } catch (e: Exception) {
        null
    }
}
