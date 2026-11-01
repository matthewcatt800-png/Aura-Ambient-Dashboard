package com.aura.ambientdashboard.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.aura.ambientdashboard.R
import com.aura.ambientdashboard.data.AuraPreferences
import com.aura.ambientdashboard.databinding.FragmentSettingsBinding
import com.aura.ambientdashboard.ui.AccentPalette
import com.aura.ambientdashboard.ui.console.ConsoleDialog
import com.aura.ambientdashboard.utilities.dp

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy { AuraPreferences(requireContext().applicationContext) }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                prefs.weatherUseLocation = true
            } else {
                prefs.weatherUseLocation = false
                binding.switchUseLocation.isChecked = false
                Toast.makeText(
                    requireContext(),
                    "Location not granted. Pick a city instead.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupTheme()
        setupAccent()
        setupSliders()
        setupClock()
        setupAmbient()
        setupMonitoring()
        setupWeather()
        setupGeneral()
    }

    private fun setupTheme() {
        when (prefs.themeMode) {
            "dark" -> binding.toggleTheme.check(R.id.btn_theme_dark)
            "light" -> binding.toggleTheme.check(R.id.btn_theme_light)
            else -> binding.toggleTheme.check(R.id.btn_theme_system)
        }
        binding.toggleTheme.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                R.id.btn_theme_dark -> "dark"
                R.id.btn_theme_light -> "light"
                else -> "system"
            }
            if (mode != prefs.themeMode) {
                prefs.themeMode = mode
                AppCompatDelegate.setDefaultNightMode(
                    when (mode) {
                        "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                        "light" -> AppCompatDelegate.MODE_NIGHT_NO
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                )
            }
        }
    }

    private fun setupAccent() {
        val nightMode = AccentPalette.isNightMode(resources.configuration)
        binding.accentDots.removeAllViews()
        AccentPalette.names.forEach { name ->
            val dot = View(requireContext())
            dot.layoutParams = LinearLayout.LayoutParams(dp(26), dp(26)).apply {
                marginEnd = dp(10)
            }
            dot.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(AccentPalette.color(name, nightMode))
                if (name == prefs.accent) {
                    setStroke(dp(2), ContextCompat.getColor(requireContext(), R.color.aura_text_primary))
                }
            }
            dot.setOnClickListener {
                prefs.accent = name
                setupAccent()
            }
            binding.accentDots.addView(dot)
        }
    }

    private fun setupSliders() {
        binding.sliderCardAlpha.value = prefs.cardAlpha.coerceIn(0.25f, 1f)
        binding.sliderCardAlpha.addOnChangeListener { _, value, fromUser ->
            if (fromUser) prefs.cardAlpha = value
        }
        binding.sliderAnimation.value = prefs.animationIntensity.coerceIn(0.4f, 1.4f)
        binding.sliderAnimation.addOnChangeListener { _, value, fromUser ->
            if (fromUser) prefs.animationIntensity = value
        }
    }

    private fun setupClock() {
        binding.toggleHour.check(if (prefs.is24Hour) R.id.btn_hour_24 else R.id.btn_hour_12)
        binding.toggleHour.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) prefs.is24Hour = checkedId == R.id.btn_hour_24
        }

        when (prefs.clockStyle) {
            "light" -> binding.toggleClockStyle.check(R.id.btn_style_light)
            "serif" -> binding.toggleClockStyle.check(R.id.btn_style_serif)
            "mono" -> binding.toggleClockStyle.check(R.id.btn_style_mono)
            else -> binding.toggleClockStyle.check(R.id.btn_style_classic)
        }
        binding.toggleClockStyle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            prefs.clockStyle = when (checkedId) {
                R.id.btn_style_light -> "light"
                R.id.btn_style_serif -> "serif"
                R.id.btn_style_mono -> "mono"
                else -> "classic"
            }
        }

        binding.switchSeconds.isChecked = prefs.showSeconds
        binding.switchSeconds.setOnCheckedChangeListener { _, checked -> prefs.showSeconds = checked }

        binding.sliderClockSize.value = prefs.clockSizeSp.coerceIn(40, 96).toFloat()
        binding.sliderClockSize.addOnChangeListener { _, value, fromUser ->
            if (fromUser) prefs.clockSizeSp = value.toInt()
        }
    }

    private fun setupAmbient() {
        when (prefs.ambientBackground) {
            "amoled" -> binding.toggleAmbientBg.check(R.id.btn_bg_amoled)
            "dusk" -> binding.toggleAmbientBg.check(R.id.btn_bg_dusk)
            else -> binding.toggleAmbientBg.check(R.id.btn_bg_midnight)
        }
        binding.toggleAmbientBg.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            prefs.ambientBackground = when (checkedId) {
                R.id.btn_bg_amoled -> "amoled"
                R.id.btn_bg_dusk -> "dusk"
                else -> "midnight"
            }
        }

        when (prefs.ambientClockPosition) {
            "top" -> binding.toggleAmbientPos.check(R.id.btn_pos_top)
            "bottom" -> binding.toggleAmbientPos.check(R.id.btn_pos_bottom)
            else -> binding.toggleAmbientPos.check(R.id.btn_pos_center)
        }
        binding.toggleAmbientPos.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            prefs.ambientClockPosition = when (checkedId) {
                R.id.btn_pos_top -> "top"
                R.id.btn_pos_bottom -> "bottom"
                else -> "center"
            }
        }

        binding.switchParticles.isChecked = prefs.ambientParticles
        binding.switchParticles.setOnCheckedChangeListener { _, checked -> prefs.ambientParticles = checked }

        binding.switchAmbientInfo.isChecked = prefs.ambientShowInfo
        binding.switchAmbientInfo.setOnCheckedChangeListener { _, checked -> prefs.ambientShowInfo = checked }

        binding.sliderGlow.value = prefs.ambientGlow.coerceIn(0f, 1f)
        binding.sliderGlow.addOnChangeListener { _, value, fromUser ->
            if (fromUser) prefs.ambientGlow = value
        }
        binding.sliderAmbientSpeed.value = prefs.ambientSpeed.coerceIn(0f, 1f)
        binding.sliderAmbientSpeed.addOnChangeListener { _, value, fromUser ->
            if (fromUser) prefs.ambientSpeed = value
        }
        binding.sliderParticles.value = prefs.ambientParticleIntensity.coerceIn(0f, 1f)
        binding.sliderParticles.addOnChangeListener { _, value, fromUser ->
            if (fromUser) prefs.ambientParticleIntensity = value
        }
        binding.switchReduce.isChecked = prefs.reduceAnimations
        binding.switchReduce.setOnCheckedChangeListener { _, checked -> prefs.reduceAnimations = checked }
    }

    private fun setupMonitoring() {
        binding.switchPauseMonitor.isChecked = prefs.monitoringPaused
        binding.switchPauseMonitor.setOnCheckedChangeListener { _, checked -> prefs.monitoringPaused = checked }

        when (prefs.monitoringIntervalSec) {
            1 -> binding.toggleInterval.check(R.id.btn_interval_1)
            5 -> binding.toggleInterval.check(R.id.btn_interval_5)
            10 -> binding.toggleInterval.check(R.id.btn_interval_10)
            else -> binding.toggleInterval.check(R.id.btn_interval_2)
        }
        binding.toggleInterval.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            prefs.monitoringIntervalSec = when (checkedId) {
                R.id.btn_interval_1 -> 1
                R.id.btn_interval_5 -> 5
                R.id.btn_interval_10 -> 10
                else -> 2
            }
        }
    }

    private fun setupWeather() {
        binding.inputCity.setText(prefs.weatherCity)
        binding.switchUseLocation.isChecked = prefs.weatherUseLocation
        binding.switchUseLocation.setOnCheckedChangeListener { _, checked ->
            if (!checked) {
                prefs.weatherUseLocation = false
            } else {
                val granted = ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    prefs.weatherUseLocation = true
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            }
        }
        binding.btnApplyCity.setOnClickListener {
            val city = binding.inputCity.text?.toString()?.trim().orEmpty()
            if (city.isNotEmpty()) {
                prefs.weatherCity = city
                prefs.weatherUseLocation = false
                binding.switchUseLocation.isChecked = false
                Toast.makeText(requireContext(), "Weather set to $city", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupGeneral() {
        binding.txtVersion.text = "AURA ${versionName()}"
        binding.btnConsole.setOnClickListener {
            (requireActivity() as? AppCompatActivity)?.let { ConsoleDialog(it).show() }
        }
        binding.btnResetDashboard.setOnClickListener {
            prefs.resetDashboard()
            Toast.makeText(requireContext(), "Dashboard layout reset", Toast.LENGTH_SHORT).show()
        }
    }

    private fun versionName(): String = try {
        requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName ?: "?"
    } catch (e: Exception) {
        "?"
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
