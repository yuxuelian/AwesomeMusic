package com.kaibo.music.activity.base

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.CallSuper
import com.kaibo.core.activity.BaseActivity
import com.kaibo.core.bus.RxBus
import com.kaibo.core.util.blur
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.play.PlayDurationBean
import com.kaibo.music.play.PlaySeekBean
import com.kaibo.music.play.PlayerService
import com.kaibo.music.play.PlayerStatusBinder
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
                val second = Math.ceil(this / 1000.0).toInt()
                String.format("%02d:%02d", second / 60, second % 60)
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName) {
            // 解除绑定时会调用这个方法
        }

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            // 绑定成功会调用这个方法
            iBinder as PlayerStatusBinder
            // 绑定成功后获取一次PlayerService的播放状态
            updateDuration(iBinder.duration)
            updateSeek(iBinder.seek)
        }
    }

    @CallSuper
    override fun initOnCreate(savedInstanceState: Bundle?) {
        // 启动播放音乐的Service
        bindService(Intent(this, PlayerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        startService<PlayerService>()
        // 订阅进度总进度监听
        RxBus.toObservable<PlayDurationBean>().`as`(bindLifecycle()).subscribe({
            updateDuration(it.duration)
        }) {
            it.printStackTrace()
        }
        RxBus.toObservable<PlaySeekBean>().`as`(bindLifecycle()).subscribe({
            updateSeek(it.seek)
        }) {
            it.printStackTrace()
        }
    }

    override fun onDestroy() {
        this.unbindService(serviceConnection)
        super.onDestroy()
    }

    protected abstract fun updateDuration(duration: Int)
    protected abstract fun updateSeek(seek: Int)

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