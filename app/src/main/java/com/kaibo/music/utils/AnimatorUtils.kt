package com.kaibo.music.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator

/**
 * @author kaibo
 * @date 2018/10/30 15:23
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

object AnimatorUtils {

    fun getRotateAnimator(targetView: View): Animator {
        return ObjectAnimator.ofFloat(targetView, "rotation", 0f, 360f).apply {
            duration = 20000L
            repeatCount = Animation.INFINITE
            interpolator = LinearInterpolator()
        }
    }

}