package com.aura.ambientdashboard.ui.system

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.aura.ambientdashboard.R
import com.aura.ambientdashboard.data.DeviceIdentity
import com.aura.ambientdashboard.data.DeviceMonitor
import com.aura.ambientdashboard.databinding.FragmentSystemBinding
import com.aura.ambientdashboard.utilities.Formatting
import com.aura.ambientdashboard.utilities.dp
import java.util.Locale
import java.util.TimeZone

class SystemFragment : Fragment() {

    private var _binding: FragmentSystemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populate()
    }

    private fun populate() {
        val container = binding.systemContainer
        container.removeAllViews()

        addSection("Device")
        addRow("Manufacturer", DeviceIdentity.manufacturer)
        addRow("Model", DeviceIdentity.model)
        addRow("Android version", DeviceIdentity.androidVersion)
        addRow("Kernel", DeviceIdentity.kernelVersion)
        addRow("Build", DeviceIdentity.buildDisplay)
        addWide("Build fingerprint", DeviceIdentity.fingerprint)

        addSection("Processor")
        addRow("Cores", DeviceIdentity.cpuCoreCount().toString())
        addRow("Model", DeviceIdentity.cpuModel())
        addRow("Max frequency", DeviceIdentity.cpuMaxFrequencyMhz()?.let { "$it MHz" } ?: "Unavailable")

        val snapshot = DeviceMonitor.snapshot(requireContext())
        addSection("Memory and storage")
        addRow("RAM", "${snapshot.totalMemMb} MB")
        addRow(
            "In use",
            "${snapshot.usedMemMb} MB (${Formatting.percent(snapshot.usedMemMb, snapshot.totalMemMb)}%)"
        )
        addRow("Internal storage", "${snapshot.totalStorageGb} GB")
        addRow("Free", "${snapshot.freeStorageGb} GB")

        addSection("Display")
        addRow("Resolution", resolutionText())
        addRow("Density", densityText())
        addRow(
            "Refresh rate",
            String.format(Locale.UK, "%.0f Hz", snapshot.refreshRateHz)
        )
        addWide("Supported modes", supportedModesText())

        addSection("System")
        addRow("Uptime", Formatting.uptime(snapshot.uptimeMs))
        addRow("Time zone", TimeZone.getDefault().displayName)
    }

    private fun resolutionText(): String = try {
        val metrics = requireContext().resources.displayMetrics
        "${metrics.widthPixels} x ${metrics.heightPixels}"
    } catch (e: Exception) {
        "Unavailable"
    }

    private fun densityText(): String = try {
        val metrics = requireContext().resources.displayMetrics
        "${metrics.densityDpi} dpi (${String.format(Locale.UK, "%.1fx", metrics.density)})"
    } catch (e: Exception) {
        "Unavailable"
    }

    private fun supportedModesText(): String = try {
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) requireContext().display else null
        display?.supportedModes
            ?.take(6)
            ?.joinToString(" \u00b7 ") { mode ->
                "${mode.physicalWidth}x${mode.physicalHeight} @ " +
                    String.format(Locale.UK, "%.0f", mode.refreshRate) + " Hz"
            }
            ?: "Unavailable"
    } catch (e: Exception) {
        "Unavailable"
    }

    private fun addSection(title: String) {
        val label = TextView(requireContext()).apply {
            text = title.uppercase(Locale.UK)
            textSize = 11f
            letterSpacing = 0.12f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.aura_accent_cyan))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(22)
                bottomMargin = dp(6)
            }
        }
        binding.systemContainer.addView(label)
    }

    private fun addRow(label: String, value: String) {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        val labelText = TextView(requireContext()).apply {
            text = label
            textSize = 13f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.aura_text_secondary))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val valueText = TextView(requireContext()).apply {
            text = value
            textSize = 13f
            gravity = Gravity.END
            setTextColor(ContextCompat.getColor(requireContext(), R.color.aura_text_primary))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(labelText)
        row.addView(valueText)
        binding.systemContainer.addView(row)
    }

    private fun addWide(label: String, value: String) {
        val labelText = TextView(requireContext()).apply {
            text = label
            textSize = 12f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.aura_text_secondary))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(10) }
        }
        val valueText = TextView(requireContext()).apply {
            text = value
            textSize = 12f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.aura_text_primary))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(2) }
        }
        binding.systemContainer.addView(labelText)
        binding.systemContainer.addView(valueText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
