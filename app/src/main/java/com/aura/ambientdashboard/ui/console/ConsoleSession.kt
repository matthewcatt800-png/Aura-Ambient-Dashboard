package com.aura.ambientdashboard.ui.console

import android.content.Context
import android.os.SystemClock
import com.aura.ambientdashboard.data.DeviceIdentity
import com.aura.ambientdashboard.data.DeviceMonitor
import com.aura.ambientdashboard.data.NetworkMonitor
import com.aura.ambientdashboard.utilities.ClockFormatter
import com.aura.ambientdashboard.utilities.Formatting
import java.time.LocalDateTime
import java.util.Locale

object ConsoleSession {

    const val CLEAR_SIGNAL = "\u0000CLEAR"

    fun execute(context: Context, raw: String): List<String> {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return emptyList()
        val command = trimmed.substringBefore(' ').lowercase(Locale.UK)
        val argument = trimmed.substringAfter(' ', "").trim()
        return when (command) {
            "help" -> listOf(
                "info          device summary",
                "time          current date and time",
                "uptime        time since boot",
                "mem           memory usage",
                "net           network state",
                "battery       charge level",
                "weather CITY  current conditions (network)",
                "echo TEXT     print text back",
                "clear         wipe this console"
            )
            "info" -> listOf(
                DeviceIdentity.manufacturer,
                DeviceIdentity.model,
                DeviceIdentity.androidVersion
            )
            "time" -> {
                val now = LocalDateTime.now()
                listOf(ClockFormatter.longDate(now), ClockFormatter.time(now, true, true))
            }
            "uptime" -> listOf("Up " + Formatting.uptime(SystemClock.elapsedRealtime()))
            "mem" -> {
                val snapshot = DeviceMonitor.snapshot(context)
                val used = Formatting.percent(snapshot.usedMemMb, snapshot.totalMemMb)
                listOf(
                    "RAM $used% used",
                    "${snapshot.availableMemMb} MB free of ${snapshot.totalMemMb} MB"
                )
            }
            "net" -> {
                val state = NetworkMonitor.state(context)
                val address = NetworkMonitor.localIpv4Address() ?: "no local address"
                listOf(state.connectivitySummary, address)
            }
            "battery" -> listOf("${DeviceMonitor.snapshot(context).batteryPercent}% charge")
            "echo" -> listOf(argument.ifBlank { "(nothing to echo)" })
            "clear" -> listOf(CLEAR_SIGNAL)
            else -> listOf("Unknown command: $command. Try help.")
        }
    }
}
