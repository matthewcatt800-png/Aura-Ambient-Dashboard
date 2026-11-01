package com.aura.ambientdashboard.ui.widgets

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aura.ambientdashboard.R
import com.aura.ambientdashboard.data.AuraPreferences
import com.aura.ambientdashboard.data.WidgetType
import com.aura.ambientdashboard.databinding.ItemWidgetBinding
import com.aura.ambientdashboard.ui.AccentPalette
import com.aura.ambientdashboard.utilities.dp

class WidgetConfigAdapter(
    private val prefs: AuraPreferences,
    private val onStateChanged: () -> Unit
) : RecyclerView.Adapter<WidgetConfigAdapter.Holder>() {

    private var entries: List<WidgetType> = emptyList()

    init {
        reload()
    }

    fun reload() {
        entries = prefs.widgetState().order
        notifyDataSetChanged()
    }

    class Holder(val binding: ItemWidgetBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemWidgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int = entries.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val type = entries[position]
        val state = prefs.widgetState()
        val context = holder.binding.root.context
        val nightMode = AccentPalette.isNightMode(context.resources.configuration)

        holder.binding.widgetTitle.text = type.title
        holder.binding.widgetDescription.text = type.description

        holder.binding.widgetSwitch.setOnCheckedChangeListener(null)
        holder.binding.widgetSwitch.isChecked = type !in state.disabled
        holder.binding.widgetSwitch.setOnCheckedChangeListener { _, checked ->
            val current = prefs.widgetState()
            val disabled = current.disabled.toMutableSet()
            if (checked) disabled.remove(type) else disabled.add(type)
            prefs.saveWidgetState(current.copy(disabled = disabled))
            onStateChanged()
        }

        holder.binding.btnMoveUp.setOnClickListener { move(position, -1) }
        holder.binding.btnMoveDown.setOnClickListener { move(position, 1) }

        holder.binding.btnStyle.text = if (type in state.plain) "Style: Plain" else "Style: Card"
        holder.binding.btnStyle.setOnClickListener {
            val current = prefs.widgetState()
            val plain = current.plain.toMutableSet()
            if (type in plain) plain.remove(type) else plain.add(type)
            prefs.saveWidgetState(current.copy(plain = plain))
            holder.binding.btnStyle.text = if (type in plain) "Style: Plain" else "Style: Card"
            onStateChanged()
        }

        holder.binding.colorDots.removeAllViews()
        AccentPalette.names.forEach { name ->
            holder.binding.colorDots.addView(
                colorDot(context, holder, name, state.colors[type] == name, position)
            )
        }
    }

    private fun colorDot(
        context: Context,
        holder: Holder,
        name: String,
        selected: Boolean,
        position: Int
    ): View {
        val nightMode = AccentPalette.isNightMode(context.resources.configuration)
        val dot = View(context)
        dot.layoutParams = ViewGroup.MarginLayoutParams(holder.binding.root.dp(20), holder.binding.root.dp(20)).apply {
            marginEnd = holder.binding.root.dp(6)
        }
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(AccentPalette.color(name, nightMode))
        }
        if (selected) {
            drawable.setStroke(holder.binding.root.dp(2), ContextCompat.getColor(context, R.color.aura_text_primary))
        }
        dot.background = drawable
        dot.setOnClickListener {
            val current = prefs.widgetState()
            val colors = current.colors.toMutableMap()
            if (selected) colors.remove(current.order[position]) else colors[current.order[position]] = name
            prefs.saveWidgetState(current.copy(colors = colors))
            onStateChanged()
            notifyItemChanged(position)
        }
        return dot
    }

    private fun move(position: Int, delta: Int) {
        val target = position + delta
        if (target < 0 || target >= entries.size) return
        val mutable = entries.toMutableList()
        val item = mutable.removeAt(position)
        mutable.add(target, item)
        entries = mutable
        prefs.saveWidgetState(prefs.widgetState().copy(order = mutable))
        notifyItemMoved(position, target)
        onStateChanged()
    }
}
