package com.yishi.refresh

import android.graphics.PointF
import android.view.animation.Interpolator

/**
 * @author 56896
 * @date 2018/10/18 23:54
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class BezierInterpolator(x1: Float, y1: Float, x2: Float, y2: Float) : Interpolator {
    private val point1 = PointF()
    private val point2 = PointF()

    private var mLastI = 0

    init {
        point1.x = x1
        point1.y = y1
        point2.x = x2
        point2.y = y2
    }

    constructor() : this(.25f, .46f, .45f, .94f)

    override fun getInterpolation(input: Float): Float {
        var t = input
        //如果重新开始要重置缓存的i。
        if ((input * 10).toInt() == 0) {
            mLastI = 0
        }
        // 近似求解t
        var tempX: Double
        for (i in mLastI..4095) {
            t = i * STEP_SIZE
            tempX = cubicEquation(t.toDouble(), point1.x.toDouble(), point2.x.toDouble())
            if (tempX >= input) {
                mLastI = i
                break
            }
        }
        val value = cubicEquation(t.toDouble(), point1.y.toDouble(), point2.y.toDouble())
        return value.toFloat()
    }

    companion object {

        private const val STEP_SIZE = 1.0f / 4096

        fun cubicEquation(t: Double, p1: Double, p2: Double): Double {
            val u = 1 - t
            val tt = t * t
            val uu = u * u
            val ttt = tt * t
            return 3.0 * uu * t * p1 + 3.0 * u * tt * p2 + ttt
        }
    }
}

