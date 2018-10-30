package com.kaibo.music.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.util.*
import com.kaibo.music.R
import com.kaibo.music.activity.base.BasePlayerActivity
import com.kaibo.music.bean.SongBean
import com.kaibo.music.player.manager.PlayManager
import com.kaibo.music.player.manager.PlayModeManager
import com.kaibo.music.utils.AnimatorUtils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.include_play_bottom.*
import kotlinx.android.synthetic.main.include_play_top.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit


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

    /**
     * 旋转动画
     */
    private val rotateAnimator by lazy {
        AnimatorUtils.getRotateAnimator(playRotaImg)
    }

    private var currentSongBean: SongBean? = null
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

        // 模糊背景图(初始化模糊一张)
        Observable
                .create<Bitmap> {
                    it.onNext(BitmapFactory.decodeResource(resources, R.drawable.test_play_img).blur(this))
                }
                .subscribeOn(Schedulers.io())
                .toMainThread()
                .`as`(bindLifecycle())
                .subscribe {
                    blurBackGround.setImageBitmap(it)
                }

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

        // 点击播放暂停按钮
        playOrPauseBtn.clicks().`as`(bindLifecycle()).subscribe {
            PlayManager.playPause()
            playOrPauseBtn.setImageResource(if (PlayManager.isPlaying) {
                R.drawable.big_pause
            } else {
                R.drawable.big_play

            })
        }

        // 播放上一曲
        prePlay.clicks().`as`(bindLifecycle()).subscribe {
            PlayManager.prev()
        }

        // 播放下一曲
        nextPlay.clicks().`as`(bindLifecycle()).subscribe {
            PlayManager.next()
        }

        // 更新播放模式
        prePlay.clicks().`as`(bindLifecycle()).subscribe {
            PlayModeManager.updatePlayMode()
        }

        // 点击收藏按钮
        collectionBtn.clicks().`as`(bindLifecycle()).subscribe {
            // TODO 收藏到我的喜欢列表
        }
    }

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

            // 切换歌曲时 旋转动画复位
            playRotaImg.rotation = 0f
            rotateAnimator.cancel()

            val blurTempFile = File(filesDir, "blur-temp-file.png")
            // 修改背景
            Observable
                    .create<Bitmap> {
                        try {
                            val bitmap = GlideApp.with(this).asBitmap().load(playingMusic.image).submit().get()
                            // 先把图片保存到本地  然后再从本地读取图片进行模糊  否则不能模糊成功
                            bitmap.saveToFile(blurTempFile)
                            it.onNext(bitmap)
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
                        // 从本地获取图片然后进行模糊
                        BitmapFactory.decodeStream(FileInputStream(blurTempFile)).blur(this)
                    }
                    .toMainThread()
                    .`as`(bindLifecycle())
                    .subscribe({
                        // 设置背景
                        blurBackGround.startTransition(it)
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
            // 获取播放状态并记录状态
            isPlaying = PlayManager.isPlaying

            // 播放状态发生改变
            playOrPauseBtn.setImageResource(if (isPlaying) {
                R.drawable.big_pause
            } else {
                R.drawable.big_play
            })

            if (isPlaying) {
                // 启动旋转动画
                if (rotateAnimator.isPaused) {
                    rotateAnimator.resume()
                } else {
                    rotateAnimator.start()
                }
            } else {
                // 取消旋转动画
                rotateAnimator.pause()
            }
        }
    }

    override fun onDestroy() {
        // 退出的时候停止旋转动画
        if (rotateAnimator.isRunning) {
            rotateAnimator.cancel()
        }
        super.onDestroy()
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
        // 滑动歌词页
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

    /**
     * 修改指示器状态
     */
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