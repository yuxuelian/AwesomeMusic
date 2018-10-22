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
import kotlinx.android.synthetic.main.include_play_bottom.*
import kotlinx.android.synthetic.main.include_play_top.*
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import java.util.concurrent.TimeUnit

/**
 * @author kaibo
 * @createDate 2018/10/15 16:18
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class PlayerActivity : BasePlayerActivity() {

    private var isPlaying = false

    override fun getLayoutRes() = R.layout.activity_player

    override fun initOnCreate(savedInstanceState: Bundle?) {
        super.initOnCreate(savedInstanceState)
        playRootView.setPadding(0, statusBarHeight, 0, 0)
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
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // 拖动的同时修改时间显示
                    currentSeek.text = progress.formatMunite()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isSeeking = false
                // 松手后发送进度到Service
//                RxBus.post(SeekCommand(seekBar.progress))
            }
        })

        val playDataSource = "https://music.kaibo123.com/amobile.music.tc.qq.com/C400003OU9ul1LEU9T.m4a?guid=4715368380&vkey=F3DA36B2E856E4F87A02E2B55C27714B0DF3540E9E6CEBDB51337D852F2C9EDB17FAFE803BDFABD97FD19DE24BC2A8D0C68D2A9DCEE6A74A&uin=0&fromtag=999"
//        val playCommand = DataSourceCommand(SongBean(url = playDataSource))

        // 播放或者暂停
        playOrPauseBtn.clicks().`as`(bindLifecycle()).subscribe {
            if (isPlaying) {
                isPlaying = false
                // 显示暂停图标
                playOrPauseBtn.setImageResource(R.drawable.big_play)
            } else {
                isPlaying = true
                // 显示播放图标
                playOrPauseBtn.setImageResource(R.drawable.big_pause)
            }
            // 发送命令
//            RxBus.post(playCommand)
        }
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
                            lrcLayout = layoutInflater.inflate(R.layout.item_lrc, container, false)
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

    override fun playStatusChange(playing: Boolean) {
        if (playing) {
            playOrPauseBtn.setImageResource(R.drawable.big_pause)
        } else {
            playOrPauseBtn.setImageResource(R.drawable.big_play)
        }
        isPlaying = playing
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