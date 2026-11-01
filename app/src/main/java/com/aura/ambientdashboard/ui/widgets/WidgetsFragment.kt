package com.aura.ambientdashboard.ui.widgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aura.ambientdashboard.data.AuraPreferences
import com.aura.ambientdashboard.databinding.FragmentWidgetsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class WidgetsFragment : Fragment() {

    private var _binding: FragmentWidgetsBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy { AuraPreferences(requireContext().applicationContext) }
    private lateinit var adapter: WidgetConfigAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWidgetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = WidgetConfigAdapter(prefs) { }
        binding.widgetList.layoutManager = LinearLayoutManager(requireContext())
        binding.widgetList.adapter = adapter

        binding.btnSaveLayout.setOnClickListener { promptSaveLayout() }
        binding.btnLoadLayout.setOnClickListener { promptLoadLayout() }
        binding.btnResetLayout.setOnClickListener {
            prefs.resetDashboard()
            adapter.reload()
        }
    }

    private fun promptSaveLayout() {
        val context = requireContext()
        val input = TextInputEditText(context).apply { hint = "Layout name" }
        val pad = (20 * resources.displayMetrics.density).toInt()
        val frame = FrameLayout(context).apply {
            setPadding(pad, (8 * resources.displayMetrics.density).toInt(), pad, 0)
            addView(input)
        }
        MaterialAlertDialogBuilder(context)
            .setTitle("Save layout")
            .setView(frame)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text?.toString()?.trim().orEmpty()
                if (name.isNotEmpty()) {
                    prefs.saveLayout(name)
                    Toast.makeText(context, "Saved \u201c$name\u201d", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun promptLoadLayout() {
        val names = prefs.savedLayoutNames()
        if (names.isEmpty()) {
            Toast.makeText(requireContext(), "No saved layouts yet", Toast.LENGTH_SHORT).show()
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Load layout")
            .setItems(names.toTypedArray()) { _, which ->
                if (prefs.loadLayout(names[which])) adapter.reload()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
