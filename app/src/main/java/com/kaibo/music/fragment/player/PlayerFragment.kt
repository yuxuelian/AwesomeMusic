package com.kaibo.music.fragment.player

import android.animation.Animator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.toast.ToastUtils
import com.kaibo.core.util.*
import com.kaibo.music.R
import com.kaibo.music.bean.SongBean
import com.kaibo.music.player.manager.PlayManager
import com.kaibo.music.utils.AnimatorUtils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.include_play_bottom.*
import kotlinx.android.synthetic.main.include_play_top.*
import org.jetbrains.anko.matchParent
import java.io.File
import java.io.FileInputStream

/**
 * @author kaibo
 * @date 2018/11/1 9:48
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class PlayerFragment : BasePlayerFragment() {

    companion object {
        fun newInstance(arguments: Bundle = Bundle()): PlayerFragment {
            return PlayerFragment().apply {
                this.arguments = arguments
            }
        }
    }

    override val isCanSwipeBack: Boolean = true

    override fun getLayoutRes() = R.layout.fragment_player

    /**
     * 当前是否正在拖动
     */
    private var isSeeking = false

    /**
     * 旋转动画
     */
    private var rotateAnimator: Animator? = null

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

                // 更新界面歌词
                updateLyric(value.mid)

                val blurTempFile = File(mActivity.filesDir, "blur-temp-file.png")
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
                            BitmapFactory.decodeStream(FileInputStream(blurTempFile)).blur(mActivity)
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

    private fun updateLyric(mid: String) {
        // 首先获取歌词
        Observable.create<String> {

                }

        // 刷新界面歌词


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

    override fun initViewCreated(savedInstanceState: Bundle?) {
        super.initViewCreated(savedInstanceState)
        playRootView.setPadding(0, mActivity.statusBarHeight, 0, 0)
        // 点击返回键
        backBtn.easyClick(bindLifecycle()).subscribe {
            onBackPressedSupport()
        }

        // 点击图片   显示歌词页
        playRotaImg.easyClick(bindLifecycle()).subscribe {
            lrcPager.currentItem = 1
        }

        playTopLayout.postDelayed({
            // 执行进入动画
            playTopLayout.startAnimation(topLayoutIn)
            playBottomLayout.startAnimation(bottomLayoutIn)
            minLrcLayout.startAnimation(alpha01)
        }, 100)

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
        }
    }


    override fun onDestroyView() {
        // 退出的时候停止旋转动画
        if (rotateAnimator != null && rotateAnimator!!.isRunning) {
            rotateAnimator?.cancel()
        }
        super.onDestroyView()
    }

    private fun initLrcLayout() {
        lrcPager.adapter = object : PagerAdapter() {
            // 加载歌词布局
            private var lrcLayout: View? = null

            private val emptyLayout by lazy {
                FrameLayout(mActivity).apply {
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

    override fun onBackPressedSupport(): Boolean {
        // 执行退出动画
        playTopLayout.startAnimation(topLayoutOut)
        playBottomLayout.startAnimation(bottomLayoutOut)
        minLrcLayout.startAnimation(alpha10)
        playTopLayout.postDelayed({
            pop()
        }, 200)
        return true
    }
}