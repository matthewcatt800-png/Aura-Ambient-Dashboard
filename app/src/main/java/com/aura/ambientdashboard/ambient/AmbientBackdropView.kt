package com.aura.ambientdashboard.ambient

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class AmbientBackdropView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var palette = AmbientPalette.MIDNIGHT
    private var driftSpeed = 0.5f
    private var glow = 0.6f
    private var animated = true
    private var aggregatedVisible = true
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun configure(background: String, speed: Float, glowIntensity: Float, reduceAnimations: Boolean) {
        palette = when (background) {
            "amoled" -> AmbientPalette.AMOLED
            "dusk" -> AmbientPalette.DUSK
            else -> AmbientPalette.MIDNIGHT
        }
        driftSpeed = 0.2f + speed * 1.3f
        glow = glowIntensity
        animated = !reduceAnimations && speed > 0.01f
        invalidate()
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        aggregatedVisible = isVisible
        if (isVisible && animated) postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(palette.base)
        val t = if (animated) System.currentTimeMillis() / 1000f * driftSpeed else 0f
        val w = width.toFloat()
        val h = height.toFloat()
        drawHalo(
            canvas,
            w * (0.30f + 0.12f * sin(t * 0.9f)),
            h * (0.32f + 0.08f * cos(t * 0.7f)),
            max(w, h) * 0.85f,
            palette.haloA,
            0.5f + glow * 0.5f
        )
        drawHalo(
            canvas,
            w * (0.72f + 0.10f * cos(t * 0.6f)),
            h * (0.72f + 0.12f * sin(t * 0.5f)),
            max(w, h) * 0.7f,
            palette.haloB,
            0.4f + glow * 0.6f
        )
        if (animated && aggregatedVisible) postInvalidateOnAnimation()
    }

    private fun drawHalo(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int, strength: Float) {
        val alpha = (Color.alpha(color) * strength).toInt().coerceIn(0, 255)
        val scaled = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        paint.shader = RadialGradient(
            cx, cy, radius, intArrayOf(scaled, Color.TRANSPARENT), null, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, paint)
    }
}
