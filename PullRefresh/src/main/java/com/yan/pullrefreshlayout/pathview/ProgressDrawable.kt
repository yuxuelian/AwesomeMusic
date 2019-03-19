package com.yishi.refresh.pathview

import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator

/**
 * @author 56896
 * @date 2018/10/18 23:54
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class ProgressDrawable : Drawable(), Animatable {

    private var mProgressDegree = 0

    /**
     * 初始化属性动画
     */
    private val mValueAnimator: ValueAnimator = ValueAnimator.ofInt(30, 3600).apply {
        duration = 10000
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        // 设置监听
        addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            mProgressDegree = 30 * (value / 30)
            invalidateSelf()
        }
    }

    private val mPath = Path()

    private val mPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = -0XCFCFD0
    }

    fun setColor(color: Int) {
        mPaint.color = color
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val width = bounds.width()
        val height = bounds.height()
        canvas.save()
        canvas.rotate(mProgressDegree.toFloat(), (width / 2).toFloat(), (height / 2).toFloat())
        val r = Math.max(1, width / 20)
        repeat(12) { index ->
            mPath.reset()
            mPath.addCircle((width - r).toFloat(), (height / 2).toFloat(), r.toFloat(), Path.Direction.CW)
            mPath.addRect((width - 5 * r).toFloat(), (height / 2 - r).toFloat(), (width - r).toFloat(), (height / 2 + r).toFloat(), Path.Direction.CW)
            mPath.addCircle((width - 5 * r).toFloat(), (height / 2).toFloat(), r.toFloat(), Path.Direction.CW)
            mPaint.alpha = (index + 5) * 0x11
            canvas.rotate(30f, (width / 2).toFloat(), (height / 2).toFloat())
            canvas.drawPath(mPath, mPaint)
        }
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun start() {
        if (!mValueAnimator.isRunning) {
            mValueAnimator.start()
        }
    }

    override fun stop() {
        if (mValueAnimator.isRunning) {
            mValueAnimator.cancel()
        }
    }

    override fun isRunning(): Boolean {
        return mValueAnimator.isRunning
    }

    fun width(): Int {
        return bounds.width()
    }

    fun height(): Int {
        return bounds.height()
    }
}
