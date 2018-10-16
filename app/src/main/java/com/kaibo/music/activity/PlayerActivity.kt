package com.kaibo.music.activity

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.util.statusBarHeight
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.activity.base.BasePlayerActivity
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.include_play_bottom_layout.*
import kotlinx.android.synthetic.main.include_play_top_layout.*
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import java.util.concurrent.TimeUnit

/**
 * @author kaibo
 * @date 2018/10/15 16:18
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class PlayerActivity : BasePlayerActivity() {

    override fun getLayoutRes() = R.layout.activity_player

    override fun initOnCreate(savedInstanceState: Bundle?) {
        super.initOnCreate(savedInstanceState)
        playRootView.setPadding(0, statusBarHeight, 0, dip(40))
        backBtn.clicks().`as`(bindLifecycle()).subscribe {
            onBackPressed()
        }

        // 点击图片   显示歌词页
        playRotaImg.clicks().`as`(bindLifecycle()).subscribe {
            lrcPager.currentItem = 1
        }

        // 模糊背景图
        blurBitmap(BitmapFactory.decodeResource(resources, R.drawable.test_play_img))
                .subscribe({
                    playRootView.backgroundDrawable = BitmapDrawable(resources, it)
                }) {
                    it.printStackTrace()
                }

        // 初始化歌词显示页
        initLrcLayout()
        // 设置SeekBar的拖动监听
        playerSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(p0: SeekBar) {
                isSeeking = false
            }
        })
    }

    private fun initLrcLayout() {
        lrcPager.adapter = object : PagerAdapter() {
            // 加载歌词布局
            private var lrcLayout: View? = null

            private val emptyLayout by lazy {
                FrameLayout(this@PlayerActivity).apply {
                    layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
                }
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                // 这里返回的对象就是 destroyItem  方法中的最后一个参数
                return when (position) {
                    0 -> {
                        container.addView(emptyLayout)
                        emptyLayout
                    }
                    1 -> {
                        if (lrcLayout == null) {
                            lrcLayout = layoutInflater.inflate(R.layout.item_lrc_layout, container, false)
                        }
                        container.addView(lrcLayout)
                        // 上面已经判空过  lrcLayout  一定不为null
                        lrcLayout!!
                    }
                    else -> {
                        throw IllegalStateException("position error")
                    }
                }
            }

            override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
                container.removeView(any as View)
            }

            override fun isViewFromObject(view: View, any: Any) = view === any

            override fun getCount() = 2
        }
        lrcPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (positionOffset == 0f) {
                    return
                }
                playCenterLayout.alpha = 1f - positionOffset
            }

            override fun onPageSelected(position: Int) {
                selectIndicator(position)
            }
        })
    }

    private fun selectIndicator(position: Int) {
        when (position) {
            0 -> {
                // 选中0
                leftDot.layoutParams = leftDot.layoutParams.apply {
                    width = dip(20)
                }
                leftDot.setBackgroundResource(R.drawable.page_selected)
                rightDot.layoutParams = rightDot.layoutParams.apply {
                    width = dip(10)
                }
                rightDot.setBackgroundResource(R.drawable.page_noraml)
            }
            1 -> {
                // 选中1
                rightDot.layoutParams = rightDot.layoutParams.apply {
                    width = dip(20)
                }
                rightDot.setBackgroundResource(R.drawable.page_selected)
                leftDot.layoutParams = leftDot.layoutParams.apply {
                    width = dip(10)
                }
                leftDot.setBackgroundResource(R.drawable.page_noraml)
            }
            else -> {
                throw IllegalStateException("position error")
            }
        }
    }

    /**
     * 当前是否正在拖动
     */
    private var isSeeking = false

    override fun updateDuration(duration: Int) {
        // 设置总进度
        playerSeek.max = duration
        maxSeek.text = duration.formatMunite()
    }

    override fun updateSeek(seek: Int) {
        if (!isSeeking) {
            playerSeek.progress = seek
            currentSeek.text = seek.formatMunite()
        }
    }

    override fun onResume() {
        super.onResume()
        // 执行进入动画
        playTopLayout.startAnimation(topLayoutIn)
        playBottomLayout.startAnimation(bottomLayoutIn)
        minLrcLayout.startAnimation(alpha01)
    }

    override fun onBackPressed() {
        // 执行退出动画
        playTopLayout.startAnimation(topLayoutOut)
        playBottomLayout.startAnimation(bottomLayoutOut)
        minLrcLayout.startAnimation(alpha10)
        Observable
                .timer(100, TimeUnit.MILLISECONDS)
                .toMainThread()
                .`as`(bindLifecycle())
                .subscribe {
                    super.onBackPressed()
                }
    }

}