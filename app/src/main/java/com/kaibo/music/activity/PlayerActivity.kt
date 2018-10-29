package com.kaibo.music.activity

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.util.blur
import com.kaibo.core.util.statusBarHeight
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.activity.base.BasePlayerActivity
import com.kaibo.music.bean.SongBean
import com.kaibo.music.player.manager.PlayManager
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.include_play_bottom.*
import kotlinx.android.synthetic.main.include_play_top.*
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import java.util.concurrent.TimeUnit
import android.view.animation.RotateAnimation


/**
 * @author kaibo
 * @createDate 2018/10/15 16:18
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class PlayerActivity : BasePlayerActivity() {

    /**
     * 当前是否正在拖动
     */
    private var isSeeking = false

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
//        blurBitmap(BitmapFactory.decodeResource(resources, R.drawable.test_play_img))
//                .subscribeOn(Schedulers.io())
//                .toMainThread()
//                .`as`(bindLifecycle())
//                .subscribe({
//                    playRootView.backgroundDrawable = BitmapDrawable(resources, it)
//                }) {
//                    it.printStackTrace()
//                }

        // 初始化歌词显示页
        initLrcLayout()
        // 设置SeekBar的拖动监听
        playerSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // 拖动的同时修改时间显示
                    currentSeek.text = progress.formatMinute()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isSeeking = false
                // 设置播放进度
                PlayManager.seekTo(seekBar.progress)
            }
        })

        // 播放或者暂停
        playOrPauseBtn.clicks().`as`(bindLifecycle()).subscribe {
            PlayManager.playPause()
            playOrPauseBtn.setImageResource(if (PlayManager.isPlaying) {
                R.drawable.big_play
            } else {
                R.drawable.big_pause
            })
        }
    }

    private var currentSongBean: SongBean? = null
    private var isPlaying = false

    /**
     * 这个方法会被定时执行
     */
    override fun tickTask() {
        val playingMusic: SongBean? = PlayManager.playingMusic
        if (playingMusic != null && playingMusic != currentSongBean) {
            // 赋值
            currentSongBean = playingMusic
            // 修改歌曲名
            songName.text = playingMusic.songname
            singerName.text = playingMusic.singername

            // 重启一下旋转动画
            mRotateAnimation.resume()
            // 旋转位置归0
            playRotaImg.rotation = 0f

            // 修改背景
            Observable
                    .create<Bitmap> {
                        try {
                            it.onNext(GlideApp.with(this).asBitmap().load(playingMusic.image).submit().get())
                            it.onComplete()
                        } catch (e: Throwable) {
                            it.onError(e)
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .toMainThread()
                    .doOnNext {
                        // 设置到旋转的ImageView
                        playRotaImg.setImageBitmap(it)
                    }
                    .observeOn(Schedulers.io())
                    .map {
                        // 模糊图片
                        it.blur(this)
                    }
                    .toMainThread()
                    .`as`(bindLifecycle())
                    .subscribe({
                        // 设置背景
                        playRootView.backgroundDrawable = BitmapDrawable(resources, it)
                    }) {
                        it.printStackTrace()
                    }
        }

        // 更新进度(这个需要时刻更新)
        if (!isSeeking) {
            val duration = PlayManager.duration
            val currentPosition = PlayManager.currentPosition
            // 修改文字进度
            maxSeek.text = duration.formatMinute()
            currentSeek.text = currentPosition.formatMinute()
            // 修改进度条
            playerSeek.max = duration
            playerSeek.progress = currentPosition
        }

        // 播放状态发生改变
        if (isPlaying != PlayManager.isPlaying) {
            isPlaying = PlayManager.isPlaying
            if (PlayManager.isPlaying) {
                // 正在播放则旋转图片
                val rotate = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                rotate.duration = 10000
                rotate.repeatCount = Animation.INFINITE
                rotate.fillAfter = true
                rotate.interpolator = LinearInterpolator()
                playRotaImg.startAnimation(rotate)
            } else {
                playRotaImg.clearAnimation()
            }
        }
    }

    private val mRotateAnimation by lazy {
        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 10000
            repeatCount = Animation.INFINITE
            interpolator = LinearInterpolator()
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