package com.aura.ambientdashboard.ui.ambient

import android.graphics.Typeface
import android.os.BatteryManager
import android.view.Gravity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aura.ambientdashboard.ambient.AmbientPalette
import com.aura.ambientdashboard.data.AuraPreferences
import com.aura.ambientdashboard.data.DeviceMonitor
import com.aura.ambientdashboard.data.WeatherRepository
import com.aura.ambientdashboard.databinding.FragmentAmbientBinding
import com.aura.ambientdashboard.utilities.ClockFormatter
import com.aura.ambientdashboard.utilities.Formatting
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Locale

class AmbientFragment : Fragment() {

    private var _binding: FragmentAmbientBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy { AuraPreferences(requireContext().applicationContext) }
    private val weatherRepository = WeatherRepository()
    private var weatherLine: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAmbientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        hideSystemBars()
        val reduce = prefs.reduceAnimations
        val palette = when (prefs.ambientBackground) {
            "amoled" -> AmbientPalette.AMOLED
            "dusk" -> AmbientPalette.DUSK
            else -> AmbientPalette.MIDNIGHT
        }
        binding.backdrop.configure(
            background = prefs.ambientBackground,
            speed = prefs.ambientSpeed * prefs.animationIntensity,
            glowIntensity = prefs.ambientGlow,
            reduceAnimations = reduce
        )
        binding.particles.configure(
            intensity = if (prefs.ambientParticles) prefs.ambientParticleIntensity * prefs.animationIntensity else 0f,
            reduceAnimations = reduce,
            particleColor = palette.particleColor
        )

        binding.ambientContent.gravity = when (prefs.ambientClockPosition) {
            "top" -> Gravity.CENTER_HORIZONTAL or Gravity.TOP
            "bottom" -> Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            else -> Gravity.CENTER
        }
        if (prefs.ambientClockPosition == "top") {
            binding.ambientContent.setPadding(0, (72 * resources.displayMetrics.density).toInt(), 0, 0)
        } else if (prefs.ambientClockPosition == "bottom") {
            binding.ambientContent.setPadding(0, 0, 0, (96 * resources.displayMetrics.density).toInt())
        } else {
            binding.ambientContent.setPadding(0, 0, 0, 0)
        }

        binding.ambientClock.textSize = prefs.clockSizeSp * 1.35f
        binding.ambientClock.typeface = when (prefs.clockStyle) {
            "light" -> Typeface.create("sans-serif-thin", Typeface.NORMAL)
            "serif" -> Typeface.SERIF
            "mono" -> Typeface.MONOSPACE
            else -> Typeface.create("sans-serif-light", Typeface.NORMAL)
        }
        binding.ambientInfo.visibility = if (prefs.ambientShowInfo) View.VISIBLE else View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                val now = LocalDateTime.now()
                binding.ambientClock.text = ClockFormatter.time(now, prefs.is24Hour, prefs.showSeconds)
                binding.ambientDate.text = ClockFormatter.longDate(now)
                delay(1_000L)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                updateInfoLine()
                delay(5_000L)
            }
        }
        if (prefs.weatherCity.isNotBlank()) {
            viewLifecycleOwner.lifecycleScope.launch {
                weatherRepository.fetchByCity(prefs.weatherCity)?.let {
                    weatherLine = String.format(Locale.UK, "%.0f", it.temperatureC) + "C " + it.condition
                }
            }
        }
    }

    private suspend fun updateInfoLine() {
        if (!prefs.ambientShowInfo) return
        val snapshot = DeviceMonitor.snapshot(requireContext())
        val battery = requireContext().getSystemService(BatteryManager::class.java)
            ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0
        val parts = mutableListOf(
            "battery $battery%",
            String.format(Locale.UK, "%.0f", snapshot.refreshRateHz) + " Hz",
            "up " + Formatting.uptime(snapshot.uptimeMs)
        )
        weatherLine?.let { parts.add(it) }
        binding.ambientInfo.text = parts.joinToString("  \u00b7  ")
    }

    private fun hideSystemBars() {
        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemBars() {
        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
    }

    override fun onDestroyView() {
        if (_binding != null) showSystemBars()
        super.onDestroyView()
        _binding = null
    }
}
