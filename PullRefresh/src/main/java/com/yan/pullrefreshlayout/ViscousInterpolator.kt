package com.yan.pullrefreshlayout

import android.view.animation.Interpolator

class ViscousInterpolator @JvmOverloads constructor(private val currentViscousScale: Float = VISCOUS_FLUID_SCALE) : Interpolator {

    private val VISCOUS_FLUID_NORMALIZE: Float
    private val VISCOUS_FLUID_OFFSET: Float

    init {
        // must be set to 1.0 (used in viscousFluid())
        VISCOUS_FLUID_NORMALIZE = 1.0f / viscousFluid(currentViscousScale, 1.0f)
        // account for very small floating-point error
        VISCOUS_FLUID_OFFSET = 1.0f - VISCOUS_FLUID_NORMALIZE * viscousFluid(currentViscousScale, 1.0f)
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
        val interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(currentViscousScale, input)
        return if (interpolated > 0) {
            interpolated + VISCOUS_FLUID_OFFSET
        } else interpolated
    }

    companion object {
        /**
         * Controls the viscous fluid effect (how much of it).
         */
        private val VISCOUS_FLUID_SCALE = 4.5f
    }
}