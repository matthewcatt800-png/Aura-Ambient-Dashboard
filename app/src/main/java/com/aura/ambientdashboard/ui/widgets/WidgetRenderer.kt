package com.aura.ambientdashboard.ui.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.aura.ambientdashboard.data.AuraPreferences
import com.aura.ambientdashboard.data.DeviceIdentity
import com.aura.ambientdashboard.data.DeviceMonitor
import com.aura.ambientdashboard.data.NetworkMonitor
import com.aura.ambientdashboard.data.WeatherCodes
import com.aura.ambientdashboard.data.WeatherSnapshot
import com.aura.ambientdashboard.data.WidgetType
import com.aura.ambientdashboard.utilities.ClockFormatter
import com.aura.ambientdashboard.utilities.Formatting
import com.aura.ambientdashboard.utilities.dp
import com.google.android.material.R as MaterialR
import java.time.LocalDateTime
import java.util.Locale

class WidgetTile(val view: View, val refresh: () -> Unit)

class WidgetRenderer(
    private val context: Context,
    private val prefs: AuraPreferences,
    private val weatherProvider: () -> WeatherSnapshot?
) {

    fun build(type: WidgetType, accentColor: Int): WidgetTile {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }
        }

        val useCard = type !in prefs.widgetState().plain
        if (useCard) {
            val cornerRadius = container.dp(18).toFloat()
            container.background = GradientDrawable().apply {
                this.cornerRadius = cornerRadius
                setColor(applyAlpha(resolveColor(MaterialR.attr.colorSurface), prefs.cardAlpha))
            }
        }

        val titleView = TextView(context).apply {
            text = type.title.uppercase(Locale.UK)
            textSize = 10f
            letterSpacing = 0.12f
            setTextColor(resolveColor(MaterialR.attr.colorOnSurfaceVariant))
        }

        val valueView = TextView(context).apply {
            textSize = 22f
            setTextColor(accentColor)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(6) }
        }

        val detailView = TextView(context).apply {
            textSize = 12f
            setTextColor(resolveColor(MaterialR.attr.colorOnSurfaceVariant))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(2) }
        }

        container.addView(titleView)
        container.addView(valueView)
        container.addView(detailView)

        var weatherIcon: ImageView? = null
        if (type == WidgetType.WEATHER) {
            weatherIcon = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(20), dp(20)).apply { topMargin = dp(10) }
                setColorFilter(accentColor)
            }
            container.addView(weatherIcon)
        }

        var sparkline: RamSparkline? = null
        if (type == WidgetType.RAM) {
            sparkline = RamSparkline(context, accentColor).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(36)
                ).apply { topMargin = dp(8) }
            }
            container.addView(sparkline)
        }

        val refresh: () -> Unit = { render(type, valueView, detailView, weatherIcon) }
        refresh()
        return WidgetTile(container, refresh)
    }

    private fun render(
        type: WidgetType,
        valueView: TextView,
        detailView: TextView,
        weatherIcon: ImageView?
    ) {
        val now = LocalDateTime.now()
        when (type) {
            WidgetType.CLOCK -> {
                valueView.text = ClockFormatter.time(now, prefs.is24Hour, prefs.showSeconds)
                detailView.visibility = View.GONE
            }
            WidgetType.DATE -> {
                valueView.text = ClockFormatter.longDate(now)
                detailView.visibility = View.GONE
            }
            WidgetType.WEATHER -> {
                val snapshot = weatherProvider()
                if (snapshot == null) {
                    valueView.text = "No data yet"
                    detailView.text = "Pick a city in Settings"
                    weatherIcon?.visibility = View.GONE
                } else {
                    valueView.text = String.format(Locale.UK, "%.1f", snapshot.temperatureC) + "\u00b0C"
                    detailView.text = snapshot.condition + " \u00b7 " + snapshot.locationName
                    weatherIcon?.visibility = View.VISIBLE
                    weatherIcon?.setImageResource(WeatherCodes.iconRes(snapshot.weatherCode))
                }
                detailView.visibility = View.VISIBLE
            }
            WidgetType.RAM -> {
                val snapshot = DeviceMonitor.snapshot(context)
                val usedPercent = Formatting.percent(snapshot.usedMemMb, snapshot.totalMemMb)
                DeviceMonitor.pushRamSample(usedPercent)
                valueView.text = "$usedPercent% used"
                detailView.text = "${snapshot.availableMemMb} MB free of ${snapshot.totalMemMb} MB"
                detailView.visibility = View.VISIBLE
            }
            WidgetType.STORAGE -> {
                val snapshot = DeviceMonitor.snapshot(context)
                val usedGb = (snapshot.totalStorageGb - snapshot.freeStorageGb).coerceAtLeast(0)
                val usedPercent = Formatting.percent(usedGb, snapshot.totalStorageGb)
                valueView.text = "$usedPercent% used"
                detailView.text = "${snapshot.freeStorageGb} GB free of ${snapshot.totalStorageGb} GB"
                detailView.visibility = View.VISIBLE
            }
            WidgetType.CPU -> {
                valueView.text = DeviceIdentity.cpuCoreCount().toString() + " cores"
                val frequency = DeviceIdentity.cpuMaxFrequencyMhz()
                detailView.text = DeviceIdentity.cpuModel() +
                    (frequency?.let { " \u00b7 up to $it MHz" } ?: "")
                detailView.visibility = View.VISIBLE
            }
            WidgetType.WIFI -> {
                val state = NetworkMonitor.state(context)
                valueView.text = if (state.wifiConnected) "Connected" else "Not connected"
                detailView.text = when {
                    state.airplaneMode -> "Airplane mode is on"
                    state.wifiConnected -> state.wifiName ?: "Network name unavailable"
                    else -> "No Wi-Fi network"
                }
                detailView.visibility = View.VISIBLE
            }
            WidgetType.MOBILE -> {
                val state = NetworkMonitor.state(context)
                valueView.text = if (state.mobileConnected) "Active" else "Not connected"
                detailView.text = state.mobileType ?: ""
                detailView.visibility = View.VISIBLE
            }
            WidgetType.INTERNET -> {
                val state = NetworkMonitor.state(context)
                valueView.text = if (state.internetValidated) "Online" else "Offline"
                detailView.text = state.connectivitySummary
                detailView.visibility = View.VISIBLE
            }
            WidgetType.DEVICE -> {
                valueView.text = DeviceIdentity.manufacturer + " " + DeviceIdentity.model
                detailView.text = DeviceIdentity.androidVersion
                detailView.visibility = View.VISIBLE
            }
            WidgetType.UPTIME -> {
                valueView.text = Formatting.uptime(DeviceMonitor.snapshot(context).uptimeMs)
                detailView.text = "Since last boot"
                detailView.visibility = View.VISIBLE
            }
            WidgetType.NETWORK -> {
                val state = NetworkMonitor.state(context)
                valueView.text = NetworkMonitor.localIpv4Address() ?: "No local address"
                detailView.text = state.connectivitySummary
                detailView.visibility = View.VISIBLE
            }
        }
    }

    private fun applyAlpha(color: Int, alphaFraction: Float): Int =
        Color.argb(
            (255 * alphaFraction.coerceIn(0.1f, 1f)).toInt(),
            Color.red(color), Color.green(color), Color.blue(color)
        )

    private fun resolveColor(attr: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(attr, value, true)
        return value.data
    }
}
