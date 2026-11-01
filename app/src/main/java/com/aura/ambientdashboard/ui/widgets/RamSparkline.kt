package com.aura.ambientdashboard.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.view.View
import com.aura.ambientdashboard.data.DeviceMonitor
import com.aura.ambientdashboard.utilities.dp

class RamSparkline(context: Context, accent: Int) : View(context) {

    private var accent = accent
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2 * resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
        color = accent
    }

    fun setAccent(color: Int) {
        accent = color
        linePaint.color = color
        linePaint.shader = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val history = DeviceMonitor.ramHistory.toList()
        if (history.size < 2) return
        val widthF = width.toFloat()
        val heightF = height.toFloat()
        val stepX = widthF / (history.size - 1).coerceAtLeast(1)
        val path = Path()
        history.forEachIndexed { index, percent ->
            val x = index * stepX
            val y = heightF - (percent / 100f) * heightF
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        val faded = Color.argb(50, Color.red(accent), Color.green(accent), Color.blue(accent))
        linePaint.shader = LinearGradient(
            0f, 0f, widthF, 0f, accent, faded, Shader.TileMode.CLAMP
        )
        canvas.drawPath(path, linePaint)
    }
}
