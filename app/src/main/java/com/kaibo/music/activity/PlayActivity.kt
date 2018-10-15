package com.kaibo.music.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.activity.BaseActivity
import com.kaibo.core.util.blur
import com.kaibo.core.util.statusBarHeight
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.android.synthetic.main.include_play_top_layout.*
import org.jetbrains.anko.backgroundDrawable

/**
 * @author kaibo
 * @date 2018/10/15 16:18
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class PlayActivity : BaseActivity() {

    private val topLayoutIn: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.top_layout_in).apply {
            fillAfter = true
        }
    }

    private val topLayoutOut: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.top_layout_out).apply {
            fillAfter = true
        }
    }

    private val bottomLayoutIn: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.bottom_layout_in).apply {
            fillAfter = true
        }
    }

    private val bottomLayoutOut: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.bottom_layout_out).apply {
            fillAfter = true
        }
    }

    private val alpha01: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.alpha_0_1).apply {
            fillAfter = true
        }
    }

    private val alpha10: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.alpha_1_0).apply {
            fillAfter = true
        }
    }

    override fun getLayoutRes() = R.layout.activity_play

    override fun initOnCreate(savedInstanceState: Bundle?) {
        // 空出StatusBar的高度
        playTopLayout.layoutParams = playTopLayout.layoutParams.apply {
            (this as LinearLayout.LayoutParams).topMargin = statusBarHeight
        }

        backBtn.clicks().`as`(bindLifecycle()).subscribe {
            onBackPressed()
        }

        // 模糊背景图片
        Observable
                .create<Bitmap> {
                    try {
                        it.onNext(BitmapFactory
                                .decodeResource(resources, R.drawable.test_play_img)
                                .blur(this))
                        it.onComplete()
                    } catch (e: Exception) {
                        it.onError(e)
                    }
                }
                .subscribeOn(Schedulers.io())
                .toMainThread()
                .`as`(bindLifecycle())
                .subscribe({
                    playRootView.backgroundDrawable = BitmapDrawable(resources, it)
                }) {
                    it.printStackTrace()
                }

    }

    override fun onResume() {
        super.onResume()
        // 执行一下动画
        playTopLayout.startAnimation(topLayoutIn)
        playBottomLayout.startAnimation(bottomLayoutIn)
        minLrcLayout.startAnimation(alpha01)
    }

    override fun onBackPressed() {
        // 执行动画  需要反着执行
        playTopLayout.startAnimation(topLayoutOut)
        playBottomLayout.startAnimation(bottomLayoutOut)
        minLrcLayout.startAnimation(alpha10)
        super.onBackPressed()
    }

}