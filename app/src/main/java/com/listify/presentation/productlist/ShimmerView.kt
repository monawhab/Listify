package com.listify.presentation.productlist

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class ShimmerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var shimmerX = -1f
    private val animator = ValueAnimator.ofFloat(-1f, 2f).apply {
        duration = 1000
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            shimmerX = it.animatedValue as Float
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = shimmerX * w

        paint.shader = LinearGradient(
            cx - w * 0.3f, 0f, cx + w * 0.3f, h,
            intArrayOf(0xFFE0E0E0.toInt(), 0xFFF5F5F5.toInt(), 0xFFE0E0E0.toInt()),
            null, Shader.TileMode.CLAMP
        )
        val rr = RectF(0f, 0f, w, h)
        canvas.drawRoundRect(rr, 8f, 8f, paint)
    }
}
