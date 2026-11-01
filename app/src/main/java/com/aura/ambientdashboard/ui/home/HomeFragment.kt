package com.aura.ambientdashboard.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aura.ambientdashboard.data.AuraPreferences
import com.aura.ambientdashboard.data.WeatherRepository
import com.aura.ambientdashboard.data.WeatherSnapshot
import com.aura.ambientdashboard.data.WidgetType
import com.aura.ambientdashboard.databinding.FragmentHomeBinding
import com.aura.ambientdashboard.ui.AccentPalette
import com.aura.ambientdashboard.ui.console.ConsoleDialog
import com.aura.ambientdashboard.ui.widgets.WidgetRenderer
import com.aura.ambientdashboard.utilities.ClockFormatter
import com.aura.ambientdashboard.utilities.Formatting
import com.aura.ambientdashboard.data.DeviceMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy { AuraPreferences(requireContext().applicationContext) }
    private val weatherRepository = WeatherRepository()
    private var weatherSnapshot: WeatherSnapshot? = null
    private var lastWeatherFetchMs = 0L
    private val liveTiles = mutableMapOf<WidgetType, () -> Unit>()

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnConsole.setOnClickListener {
            (requireActivity() as? AppCompatActivity)?.let { ConsoleDialog(it).show() }
        }
        renderWidgets()
        applyClockAppearance()

        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                updateClock()
                delay(if (prefs.showSeconds) 250L else 1_000L)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                refreshLiveData()
                delay(prefs.monitoringIntervalSec * 1_000L)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch { refreshWeatherIfDue() }
    }

    override fun onResume() {
        super.onResume()
        renderWidgets()
        applyClockAppearance()
    }

    private fun applyClockAppearance() {
        binding.txtClock.textSize = prefs.clockSizeSp.toFloat()
        binding.txtClock.typeface = when (prefs.clockStyle) {
            "light" -> android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL)
            "serif" -> android.graphics.Typeface.SERIF
            "mono" -> android.graphics.Typeface.MONOSPACE
            else -> android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        }
    }

    private fun renderWidgets() {
        liveTiles.clear()
        binding.widgetContainer.removeAllViews()
        val state = prefs.widgetState()
        val nightMode = AccentPalette.isNightMode(resources.configuration)
        val globalAccent = AccentPalette.color(prefs.accent, nightMode)
        val renderer = WidgetRenderer(requireContext(), prefs) { weatherSnapshot }
        for (type in state.order) {
            if (type in state.disabled) continue
            val accent = state.colors[type]?.let { AccentPalette.color(it, nightMode) } ?: globalAccent
            val tile = renderer.build(type, accent)
            liveTiles[type] = tile.refresh
            binding.widgetContainer.addView(tile.view)
        }
    }

    private fun updateClock() {
        val now = LocalDateTime.now()
        binding.txtClock.text = ClockFormatter.time(now, prefs.is24Hour, prefs.showSeconds)
        binding.txtDate.text = ClockFormatter.longDate(now)
        binding.txtGreeting.text = ClockFormatter.greeting(now)
        liveTiles[WidgetType.CLOCK]?.invoke()
        liveTiles[WidgetType.DATE]?.invoke()
    }

    private fun refreshLiveData() {
        if (prefs.monitoringPaused) return
        val snapshot = DeviceMonitor.snapshot(requireContext())
        DeviceMonitor.pushRamSample(Formatting.percent(snapshot.usedMemMb, snapshot.totalMemMb))
        for (type in liveTiles.keys) {
            if (type != WidgetType.CLOCK && type != WidgetType.DATE) liveTiles[type]?.invoke()
        }
    }

    private suspend fun refreshWeatherIfDue() {
        if (!prefs.weatherUseLocation && prefs.weatherCity.isBlank()) return
        val now = System.currentTimeMillis()
        if (weatherSnapshot != null && now - lastWeatherFetchMs < 15 * 60 * 1000) return
        val snapshot = if (prefs.weatherUseLocation && hasLocationPermission()) {
            val location = lastKnownLocation()
            if (location != null) {
                weatherRepository.fetch(location.latitude, location.longitude, "Current location")
            } else {
                weatherRepository.fetchByCity(prefs.weatherCity)
            }
        } else {
            weatherRepository.fetchByCity(prefs.weatherCity)
        }
        if (snapshot != null) {
            weatherSnapshot = snapshot
            lastWeatherFetchMs = System.currentTimeMillis()
            liveTiles[WidgetType.WEATHER]?.invoke()
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun lastKnownLocation(): Location? = try {
        val manager = requireContext().getSystemService(LocationManager::class.java)
        manager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: manager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
    } catch (e: Exception) {
        null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
