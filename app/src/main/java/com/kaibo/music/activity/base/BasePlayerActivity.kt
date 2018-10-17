package com.kaibo.music.activity.base

import android.graphics.Bitmap
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.CallSuper
import com.kaibo.core.activity.BaseActivity
import com.kaibo.core.bus.RxBus
import com.kaibo.core.util.blur
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.play.*
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.startService

/**
 * @author kaibo
 * @date 2018/10/16 9:42
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

abstract class BasePlayerActivity : BaseActivity() {

    protected val topLayoutIn: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.top_layout_in).apply {
            fillAfter = true
        }
    }

    protected val topLayoutOut: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.top_layout_out).apply {
            fillAfter = true
        }
    }

    protected val bottomLayoutIn: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.bottom_layout_in).apply {
            fillAfter = true
        }
    }

    protected val bottomLayoutOut: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.bottom_layout_out).apply {
            fillAfter = true
        }
    }

    protected val alpha01: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.alpha_0_1).apply {
            fillAfter = true
        }
    }

    protected val alpha10: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.alpha_1_0).apply {
            fillAfter = true
        }
    }

    /**
     * 将毫秒数转化成字符串
     */
    protected fun Int.formatMunite(): String {
        // 5999000 99:59
        return when {
            this >= 5999000 -> "99:59"
            this <= 0 -> "00:00"
            else -> {
                val second = Math.floor(this / 1000.0).toInt()
                String.format("%02d:%02d", second / 60, second % 60)
            }
        }
    }

    @CallSuper
    override fun initOnCreate(savedInstanceState: Bundle?) {
        // 启动播放音乐的Service
        startService<PlayerService>()
        // 订阅进度总进度监听
        RxBus.toObservable<PlayerStatus>().`as`(bindLifecycle()).subscribe {
            when (it) {
                is DataSourceStatus -> {
                }
                is PlayingStatus -> {
                    when (it.status) {
                        PlayingStatus.Status.PLAY -> playStatusChange(true)
                        PlayingStatus.Status.PAUSE -> playStatusChange(false)
                        else -> {
                        }
                    }
                }
                is DurationStatus -> {
                    updateDuration(it.duration)
                }
                is SeekStatus -> {
                    updateSeek(it.seek)
                }
            }
        }
    }

    protected abstract fun updateDuration(duration: Int)
    protected abstract fun updateSeek(seek: Int)
    protected abstract fun playStatusChange(playing: Boolean)

    /**
     * 模糊背景图片
     */
    protected fun blurBitmap(bitmap: Bitmap) = Observable
            .create<Bitmap> {
                try {
                    it.onNext(bitmap.blur(this))
                    it.onComplete()
                } catch (e: Exception) {
                    it.onError(e)
                }
            }
            .subscribeOn(Schedulers.io())
            .toMainThread()
            .`as`(bindLifecycle())
}