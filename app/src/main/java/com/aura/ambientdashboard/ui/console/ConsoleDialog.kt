package com.aura.ambientdashboard.ui.console

import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aura.ambientdashboard.R
import com.aura.ambientdashboard.data.WeatherRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Locale

class ConsoleDialog(private val activity: AppCompatActivity) {

    private val repository = WeatherRepository()

    fun show() {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_console, null)
        val output = view.findViewById<TextView>(R.id.console_output)
        val input = view.findViewById<TextInputEditText>(R.id.console_input)

        output.text = "AURA console\ntype help for commands"

        fun append(lines: List<String>) {
            if (lines.isEmpty()) return
            val builder = StringBuilder(output.text ?: "")
            for (line in lines) builder.append('\n').append(line)
            var text = builder.toString()
            if (text.length > 2500) text = text.substring(text.length - 2500)
            output.text = text
        }

        fun run(raw: String) {
            if (raw.isBlank()) return
            append(listOf("> $raw"))
            val trimmed = raw.trim()
            if (trimmed.startsWith("weather", ignoreCase = true)) {
                val city = trimmed.substringAfter(' ', "").trim()
                if (city.isEmpty()) {
                    append(listOf("Usage: weather CITY"))
                } else {
                    activity.lifecycleScope.launch {
                        append(listOf("Looking up $city..."))
                        val snapshot = repository.fetchByCity(city)
                        if (snapshot != null) {
                            append(
                                listOf(
                                    String.format(Locale.UK, "%.1f", snapshot.temperatureC) + "C, " +
                                        snapshot.condition + " in " + snapshot.locationName
                                )
                            )
                        } else {
                            append(listOf("Lookup failed. Check the city name and your connection."))
                        }
                    }
                }
            } else {
                val result = ConsoleSession.execute(activity, trimmed)
                if (result.firstOrNull() == ConsoleSession.CLEAR_SIGNAL) {
                    output.text = ""
                } else {
                    append(result)
                }
            }
        }

        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                run(input.text?.toString() ?: "")
                input.setText("")
                true
            } else {
                false
            }
        }

        MaterialAlertDialogBuilder(activity)
            .setTitle("Console")
            .setView(view)
            .setPositiveButton("Close", null)
            .show()
    }
}
