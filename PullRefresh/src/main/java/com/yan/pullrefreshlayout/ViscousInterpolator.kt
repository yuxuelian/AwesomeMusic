package com.yishi.refresh

import android.view.animation.Interpolator

/**
 * @author 56896
 * @date 2018/10/18 23:54
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
internal class ViscousInterpolator constructor(private val currentViscousScale: Float = VISCOUS_FLUID_SCALE) : Interpolator {

    private val viscousFluidNormalize: Float
    private val viscousFluidOffset: Float

    init {
        // must be set to 1.0 (used in viscousFluid())
        viscousFluidNormalize = 1.0f / viscousFluid(currentViscousScale, 1.0f)
        // account for very small floating-point error
        viscousFluidOffset = 1.0f - viscousFluidNormalize * viscousFluid(currentViscousScale, 1.0f)
    }

    private fun viscousFluid(viscousScale: Float, x: Float): Float {
        var mX = x * viscousScale
        if (mX < 1.0f) {
            mX -= 1.0f - Math.exp((-mX).toDouble()).toFloat()
        } else {
            // 1/e == exp(-1)
            val start = 0.36787944117f
            mX = 1.0f - Math.exp((1.0f - mX).toDouble()).toFloat()
            mX = start + mX * (1.0f - start)
        }
        return mX
    }

    override fun getInterpolation(input: Float): Float {
        val interpolated = viscousFluidNormalize * viscousFluid(currentViscousScale, input)
        return if (interpolated > 0) {
            interpolated + viscousFluidOffset
        } else interpolated
    }

    companion object {
        /**
         * Controls the viscous fluid effect (how much of it).
         */
        private const val VISCOUS_FLUID_SCALE = 4.5f
    }
}