package com.kaibo.music.fragment.player

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * @author kaibo
 * @date 2018/11/1 10:37
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

abstract class BasePlayerFragment : BaseFragment() {

    protected val topLayoutIn: Animation by lazy {
        AnimationUtils.loadAnimation(mActivity, R.anim.top_layout_in).apply {
            fillAfter = true
        }
    }

    protected val topLayoutOut: Animation by lazy {
        AnimationUtils.loadAnimation(mActivity, R.anim.top_layout_out).apply {
            fillAfter = true
        }
    }

    protected val bottomLayoutIn: Animation by lazy {
        AnimationUtils.loadAnimation(mActivity, R.anim.bottom_layout_in).apply {
            fillAfter = true
        }
    }

    protected val bottomLayoutOut: Animation by lazy {
        AnimationUtils.loadAnimation(mActivity, R.anim.bottom_layout_out).apply {
            fillAfter = true
        }
    }

    protected val alpha01: Animation by lazy {
        AnimationUtils.loadAnimation(mActivity, R.anim.alpha_0_1).apply {
            fillAfter = true
        }
    }

    protected val alpha10: Animation by lazy {
        AnimationUtils.loadAnimation(mActivity, R.anim.alpha_1_0).apply {
            fillAfter = true
        }
    }

    /**
     * 将毫秒数转化成字符串
     */
    protected fun Int.formatMinute(): String {
        // 5999000 99:59 minute
        return when {
            this >= 5999000 -> "99:59"
            this <= 0 -> "00:00"
            else -> {
                val second = Math.floor(this / 1000.0).toInt()
                String.format("%02d:%02d", second / 60, second % 60)
            }
        }
    }

    /**
     * 每隔200ms执行一次
     */
    protected abstract fun tickTask()

    private var tickDisposable: Disposable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 启动心跳  更新UI
        tickDisposable = Observable.interval(100L, 200L, TimeUnit.MILLISECONDS).toMainThread().subscribe({
            tickTask()
        }) {
            it.printStackTrace()
        }
    }

    override fun onDestroyView() {
        tickDisposable?.dispose()
        super.onDestroyView()
    }

}