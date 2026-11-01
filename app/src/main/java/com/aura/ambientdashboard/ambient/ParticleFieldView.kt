package com.aura.ambientdashboard.ambient

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class ParticleFieldView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private class Particle(
        var x: Float, var y: Float,
        var radius: Float, var speed: Float,
        var phase: Float, var alphaScale: Float
    )

    private val particles = mutableListOf<Particle>()
    private var intensity = 0.6f
    private var animated = true
    private var aggregatedVisible = true
    private var color = 0x88BFE9F5.toInt()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun configure(intensity: Float, reduceAnimations: Boolean, particleColor: Int) {
        this.intensity = intensity
        this.animated = !reduceAnimations
        this.color = particleColor
        seed()
        invalidate()
    }

    private fun seed() {
        particles.clear()
        val count = (intensity * 46f).toInt()
        val random = Random(4711)
        repeat(count) {
            particles.add(
                Particle(
                    x = random.nextFloat(),
                    y = random.nextFloat(),
                    radius = 1f + random.nextFloat() * 2.6f,
                    speed = 0.15f + random.nextFloat() * 0.35f,
                    phase = random.nextFloat() * 2f * Math.PI.toFloat(),
                    alphaScale = 0.25f + random.nextFloat() * 0.6f
                )
            )
        }
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        aggregatedVisible = isVisible
        if (isVisible && animated) postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (particles.isEmpty()) return
        val w = width.toFloat()
        val h = height.toFloat()
        val baseAlpha = Color.alpha(color)
        paint.color = color
        for (particle in particles) {
            paint.alpha = (baseAlpha * particle.alphaScale).toInt().coerceIn(0, 255)
            canvas.drawCircle(
                particle.x * w,
                particle.y * h,
                particle.radius * resources.displayMetrics.density,
                paint
            )
        }
        if (animated && aggregatedVisible) {
            advance()
            postInvalidateOnAnimation()
        }
    }

    private fun advance() {
        for (particle in particles) {
            particle.y -= particle.speed * 0.0006f
            particle.x += 0.00012f * kotlin.math.sin(particle.phase + particle.y * 9f)
            if (particle.y < -0.05f) {
                particle.y = 1.05f
            }
            if (particle.x > 1.05f) particle.x = -0.05f
            if (particle.x < -0.05f) particle.x = 1.05f
        }
    }
}
