package com.kaibo.music.activity

import android.animation.Animator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.toast.ToastUtils
import com.kaibo.core.util.*
import com.kaibo.music.R
import com.kaibo.music.bean.LyricRowBean
import com.kaibo.music.bean.SongBean
import com.kaibo.music.player.manager.PlayManager
import com.kaibo.music.utils.AnimatorUtils
import com.stx.xhb.xbanner.transformers.BasePageTransformer
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.include_play_bottom.*
import kotlinx.android.synthetic.main.include_play_top.*
import kotlinx.android.synthetic.main.item_lrc.*
import kotlinx.android.synthetic.main.item_player.*
import org.jetbrains.anko.dip
import java.io.File
import java.io.FileInputStream

/**
 * @author kaibo
 * @date 2018/11/1 9:48
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
    private var rotateAnimator: Animator? = null

    /**
     * 歌词
     */
    private var lyricRowBeans: List<LyricRowBean>? = null

    private var currentSongBean: SongBean? = null
        set(value) {
            if (value != null && value != field) {
                // 赋值
                field = value

                // 修改歌曲名
                songName.text = value.songname
                singerName.text = value.singername

                // 切换歌曲时 旋转动画复位
                playRotaImg.rotation = 0f
                // 动画取消
                rotateAnimator?.cancel()
                val blurTempFile = File(this.filesDir, "blur-temp-file.png")
                // 修改背景
                Observable
                        .create<Bitmap> {
                            try {
                                val bitmap = GlideApp.with(this).asBitmap().load(value.image).submit().get()
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
        }

    private var isPlaying = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    if (rotateAnimator == null) {
                        rotateAnimator = AnimatorUtils.getRotateAnimator(playRotaImg)
                    }
                    // 启动旋转动画
                    if (rotateAnimator!!.isPaused) {
                        rotateAnimator!!.resume()
                    } else {
                        rotateAnimator!!.start()
                    }
                } else {
                    // 取消旋转动画
                    rotateAnimator?.pause()
                }
            }

            // 播放状态发生改变
            playOrPauseBtn.setImageResource(if (value) {
                R.drawable.big_pause
            } else {
                R.drawable.big_play
            })
        }

    override fun getLayoutRes() = R.layout.fragment_player

    private fun updateLyric(mid: String, currentPosition: Int) {
        if (lyricRowBeans == null || mid != LyricRowBean.currentLyricMid) {
            // 获取歌词
            lyricRowBeans = PlayManager.lyricRowBeans
            // 设置歌词列表
            lyricView.lyricRowBeans = lyricRowBeans
        }
        if (lyricRowBeans == null || lyricRowBeans!!.size <= 2) {
            // 暂无歌词
            centerLrc.text = "暂无歌词"
        } else {
            lyricRowBeans?.let {
                (1..it.size - 2).forEach { index ->
                    val current = it[index]
                    val next = it[index + 1]
                    if (currentPosition > current.timeMillis && currentPosition < next.timeMillis) {
                        val last = it[index - 1]
                        topLrc.text = last.rowText
                        centerLrc.text = current.rowText
                        bottomLrc.text = next.rowText

                        // 更新lyricView显示的歌词
                        lyricView.showPosition = index
                    }
                }
            }
        }
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        playRootView.setPadding(0, this.statusBarHeight, 0, 0)
        // 点击返回键
        backBtn.easyClick(bindLifecycle()).subscribe {
            onBackPressedSupport()
        }

        // 初始化歌词显示页
        initLrcLayout()

        // 执行进入动画
        playTopLayout.postDelayed({
            playTopLayout.startAnimation(topLayoutIn)
            playBottomLayout.startAnimation(bottomLayoutIn)
            minLrcLayout.startAnimation(alpha01)
        }, 100)

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
        playOrPauseBtn.easyClick(bindLifecycle()).subscribe {
            singleAsync(bindLifecycle()) {
                PlayManager.togglePlayer()
            }
        }

        // 播放上一曲
        prePlay.easyClick(bindLifecycle()).subscribe {
            singleAsync(bindLifecycle()) {
                PlayManager.prev()
            }
        }

        // 播放下一曲
        nextPlay.easyClick(bindLifecycle()).subscribe {
            singleAsync(bindLifecycle()) {
                PlayManager.next()
            }
        }

        // 更新播放模式
        changePlayMode.easyClick(bindLifecycle()).subscribe {
            singleAsync(bindLifecycle(), onSuccess = {
                ToastUtils.showSuccess(it)
            }) {
                PlayManager.updatePlayMode()
            }
        }

        // 点击收藏按钮
        collectionBtn.easyClick(bindLifecycle()).subscribe {
            // TODO 收藏到我的喜欢列表
            playTopLayout.startAnimation(topLayoutIn)
            playBottomLayout.startAnimation(bottomLayoutIn)
            minLrcLayout.startAnimation(alpha01)
        }
    }

    /**
     * 这个方法会被定时执行
     */
    override fun tickTask() {
        // 获取播放的歌曲
        currentSongBean = PlayManager.playSong
        // 获取播放状态
        isPlaying = PlayManager.isPlaying

        // 歌曲进度修改
        if (!isSeeking) {
            val duration = PlayManager.duration
            val currentPosition = PlayManager.currentPosition
            // 修改文字进度
            maxSeek.text = duration.formatMinute()
            currentSeek.text = currentPosition.formatMinute()
            // 修改进度条
            playerSeek.max = duration
            playerSeek.progress = currentPosition

            // 更新界面歌词
            if (currentSongBean != null) {
                updateLyric(currentSongBean!!.mid, currentPosition)
            }
        }
    }

    override fun onDestroy() {
        // 退出的时候停止旋转动画
        if (rotateAnimator != null && rotateAnimator!!.isRunning) {
            rotateAnimator?.cancel()
        }
        super.onDestroy()
    }

    private fun initLrcLayout() {
        // 设置适配器
        lrcPager.adapter = object : PagerAdapter() {

            private var lrcLayout: View? = null
            private var playerLayout: View? = null

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                // 这里返回的对象就是 destroyItem  方法中的最后一个参数
                return when (position) {
                    0 -> {
                        if (playerLayout == null) {
                            playerLayout = layoutInflater.inflate(R.layout.item_player, container, false)
                        }
                        container.addView(playerLayout)
                        playerLayout!!
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

        lrcPager.setPageTransformer(true, object : BasePageTransformer() {
            override fun handleLeftPage(view: View, position: Float) {
                view.translationX = -view.width * position

                // 设置缩放中心
                view.pivotX = view.width * .5f
                view.pivotY = view.height * .5f

                var scale = 1 + position
                view.alpha = scale
                if (scale < .8f) {
                    scale = .8f
                }
                view.scaleX = scale
                view.scaleY = scale
            }

            override fun handleRightPage(view: View, position: Float) {

            }

            override fun handleInvisiblePage(view: View, position: Float) {

            }
        })

        // 滑动歌词页
        lrcPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
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

    override fun onBackPressedSupport() {
        // 执行退出动画
        playTopLayout.startAnimation(topLayoutOut)
        playBottomLayout.startAnimation(bottomLayoutOut)
        minLrcLayout.startAnimation(alpha10)
        playTopLayout.postDelayed({
            super.onBackPressedSupport()
        }, 200)
    }
}