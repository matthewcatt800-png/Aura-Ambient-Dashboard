package com.aura.ambientdashboard.data

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.view.Display
import android.view.WindowManager
import com.aura.ambientdashboard.utilities.Formatting

data class DeviceSnapshot(
    val totalMemMb: Long,
    val usedMemMb: Long,
    val availableMemMb: Long,
    val totalStorageGb: Long,
    val freeStorageGb: Long,
    val uptimeMs: Long,
    val refreshRateHz: Float,
    val batteryPercent: Int
)

object DeviceMonitor {

    /** Rolling RAM usage history in percent, kept for the sparkline. Main thread only. */
    val ramHistory = ArrayDeque<Int>()

    fun pushRamSample(percent: Int) {
        ramHistory.addLast(percent)
        if (ramHistory.size > 64) ramHistory.removeFirst()
    }

    fun snapshot(context: Context): DeviceSnapshot {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memory)

        val stat = StatFs(Environment.getDataDirectory().absolutePath)

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        }

        val totalMemMb = Formatting.megabytes(memory.totalMem)
        val availableMemMb = Formatting.megabytes(memory.availMem)
        return DeviceSnapshot(
            totalMemMb = totalMemMb,
            usedMemMb = (totalMemMb - availableMemMb).coerceAtLeast(0),
            availableMemMb = availableMemMb,
            totalStorageGb = (stat.totalBytes / (1024L * 1024L * 1024L)),
            freeStorageGb = (stat.availableBytes / (1024L * 1024L * 1024L)),
            uptimeMs = SystemClock.elapsedRealtime(),
            refreshRateHz = display?.refreshRate ?: 60f,
            batteryPercent = if (battery in 0..100) battery else 0
        )
    }
}
