package com.aura.ambientdashboard.data

import org.json.JSONArray
import org.json.JSONObject

data class WidgetState(
    val order: List<WidgetType>,
    val disabled: Set<WidgetType>,
    val colors: Map<WidgetType, String>,
    val plain: Set<WidgetType>
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("order", JSONArray(order.map { it.name }))
        root.put("disabled", JSONArray(disabled.map { it.name }))
        val colorsJson = JSONObject()
        colors.forEach { (type, color) -> colorsJson.put(type.name, color) }
        root.put("colors", colorsJson)
        root.put("plain", JSONArray(plain.map { it.name }))
        return root.toString()
    }

    companion object {
        fun default(): WidgetState = WidgetState(
            order = WidgetType.entries.toList(),
            disabled = setOf(WidgetType.CPU, WidgetType.NETWORK),
            colors = emptyMap(),
            plain = emptySet()
        )

        fun fromJson(raw: String): WidgetState = try {
            val root = JSONObject(raw)
            val order = mutableListOf<WidgetType>()
            val orderJson = root.optJSONArray("order") ?: JSONArray()
            for (i in 0 until orderJson.length()) {
                runCatching { WidgetType.valueOf(orderJson.getString(i)) }
                    .onSuccess { order.add(it) }
            }
            WidgetType.entries.filterTo(order) { it !in order }
            val disabled = mutableSetOf<WidgetType>()
            val disabledJson = root.optJSONArray("disabled") ?: JSONArray()
            for (i in 0 until disabledJson.length()) {
                runCatching { WidgetType.valueOf(disabledJson.getString(i)) }
                    .onSuccess { disabled.add(it) }
            }
            val plain = mutableSetOf<WidgetType>()
            val plainJson = root.optJSONArray("plain") ?: JSONArray()
            for (i in 0 until plainJson.length()) {
                runCatching { WidgetType.valueOf(plainJson.getString(i)) }
                    .onSuccess { plain.add(it) }
            }
            val colors = mutableMapOf<WidgetType, String>()
            val colorsJson = root.optJSONObject("colors")
            if (colorsJson != null) {
                for (type in WidgetType.entries) {
                    val color = colorsJson.optString(type.name, "")
                    if (color.isNotEmpty()) colors[type] = color
                }
            }
            WidgetState(order, disabled, colors, plain)
        } catch (e: Exception) {
            default()
        }
    }
}
