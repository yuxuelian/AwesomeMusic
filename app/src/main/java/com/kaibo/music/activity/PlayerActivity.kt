package com.kaibo.music.activity

import android.animation.Animator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.lifecycle.Lifecycle
import com.kaibo.core.util.*
import com.kaibo.music.R
import com.kaibo.music.callback.BasePlayerCallbackStub
import com.kaibo.music.player.PlayerController
import com.kaibo.music.player.bean.SongBean
import com.kaibo.music.player.utils.AnimatorUtils
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.include_play_bottom.*
import kotlinx.android.synthetic.main.include_play_top.*
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * @author kaibo
 * @date 2018/11/1 9:48
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class PlayerActivity : BaseMusicActivity() {

    // 当前是否正在拖动
    private var isSeeking = false

    // 旋转动画
    private val rotateAnimator: Animator by lazy {
        AnimatorUtils.getRotateAnimator(playRotaImg)
    }

    // 远程回调
    private val playerCallbackStub = PlayerCallbackStub(this)

    override fun getLayoutRes() = R.layout.activity_player

    override fun initOnCreate(savedInstanceState: Bundle?) {
        // 延迟共享动画的执行
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition()
        }
        // 设置沉浸式
        playTopLayout.layoutParams = playTopLayout.layoutParams.let { it as LinearLayout.LayoutParams }.apply {
            this.topMargin = statusBarHeight
        }

        // 点击返回键
        backBtn.easyClick(bindLifecycle()).subscribe {
            onBackPressed()
        }

        // 设置默认的背景图片
        singleAsync(bindLifecycle(), onSuccess = {
            // 模糊图片执行成功
            blurBackGround.setImageBitmap(it)
        }) {
            // 异步模糊图片
            BitmapFactory.decodeResource(resources, R.drawable.default_cover).blur(this)
        }

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
                PlayerController.seekTo(seekBar.progress)
            }
        })

        // 点击播放暂停按钮
        playOrPauseBtn.easyClick(bindLifecycle()).subscribe {
            PlayerController.togglePlayer()
        }

        // 播放上一曲
        prePlay.easyClick(bindLifecycle()).subscribe {
            PlayerController.prev()
        }

        // 播放下一曲
        nextPlay.easyClick(bindLifecycle()).subscribe {
            PlayerController.next()
        }

        // 更新播放模式
        changePlayMode.easyClick(bindLifecycle()).subscribe {
            val playMode = PlayerController.updatePlayMode()
            Logger.d("playMode = $playMode")
        }

        // 点击收藏按钮
        collectionBtn.easyClick(bindLifecycle()).subscribe {

        }

        // 执行进入动画
        playTopLayout.postDelayed({
            playTopLayout.animate()
                    .translationY(0f).alpha(1f)
                    .setInterpolator(DecelerateInterpolator()).setDuration(400)
                    .start()
            playBottomLayout.animate()
                    .translationY(0f).alpha(1f)
                    .setInterpolator(DecelerateInterpolator()).setDuration(400)
                    .start()
            minLrcLayout.animate()
                    .alpha(1f)
                    .setInterpolator(DecelerateInterpolator()).setDuration(400)
                    .start()
        }, 500)

        bottomSheetLayout.onCollapseListener = {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // 注册播放状态回调
        PlayerController.registerCallback(playerCallbackStub)

        // 主动加载一次歌曲信息
        PlayerController.getPlaySong()?.let(::showSongInfo)
        PlayerController.getSongImage()?.let(::showSongImage)

        // 刷新一次进度
        val duration = PlayerController.getDuration()
        playerSeek.max = duration
        maxSeek.text = duration.formatMinute()
        val currentPosition = PlayerController.getCurrentPosition()
        playerSeek.progress = currentPosition
        currentSeek.text = currentPosition.formatMinute()

        if (PlayerController.isPrepared()) {
            prepareDone()
        } else {
            startPrepare()
        }

        rotateAnimator.start()
    }

    override fun onPause() {
        PlayerController.unregisterCallback(playerCallbackStub)
        rotateAnimator.pause()
        super.onPause()
    }

    override fun onDestroy() {
        if (rotateAnimator.isRunning) {
            rotateAnimator.cancel()
        }
        super.onDestroy()
    }

    override fun isSupportSwipeBack() = false

    private fun showSongInfo(songBean: SongBean) {
        songName.text = songBean.songname
        singerName.text = songBean.singername
    }

    private fun showSongImage(songImage: Bitmap) {
        // 切换歌曲时 旋转动画复位
        playRotaImg.rotation = 0f
        // 设置到旋转的ImageView
        playRotaImg.setImageBitmap(songImage)
        // 模糊操作
        singleAsync(bindLifecycle(), onSuccess = {
            // 设置背景
            blurBackGround.startTransition(it)
        }) {
            songImage.blur(this)
        }
    }

    private fun startPrepare() {
        prepareProgress.visibility = View.VISIBLE
        playOrPauseBtn.visibility = View.GONE
    }

    private fun prepareDone() {
        prepareProgress.visibility = View.GONE
        playOrPauseBtn.visibility = View.VISIBLE
        val duration = PlayerController.getDuration()
        // 获取播放总时长
        playerSeek.max = duration
        maxSeek.text = duration.formatMinute()
        // 这个心跳在界面 pause 的时候会自动停止
        Observable.interval(1000L, TimeUnit.MILLISECONDS).toMainThread()
                .`as`(bindLifecycle(Lifecycle.Event.ON_PAUSE)).subscribe({
                    if (!isSeeking) {
                        // 没有正在拖拽
                        val currentPosition = PlayerController.getCurrentPosition()
                        playerSeek.progress = currentPosition
                        currentSeek.text = currentPosition.formatMinute()
                    }
                    // TODO 更新歌词进度

                }) {
                    it.printStackTrace()
                }
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
                throw IllegalStateException("getCurrentPosition error")
            }
        }
    }

    override fun onBackPressed() {
        // 执行退出动画
        playTopLayout.animate()
                .translationY(-dip(80).toFloat()).alpha(0f)
                .setInterpolator(AccelerateInterpolator()).setDuration(400)
                .start()
        playBottomLayout.animate()
                .translationY(dip(200).toFloat()).alpha(0f)
                .setInterpolator(AccelerateInterpolator()).setDuration(400)
                .start()
        minLrcLayout.animate()
                .alpha(0f)
                .setInterpolator(AccelerateInterpolator()).setDuration(400)
                .start()
        playTopLayout.postDelayed({
            animFinish()
//            supportFinishAfterTransition()
        }, 400)
    }

    private class PlayerCallbackStub(playerActivity: PlayerActivity) : BasePlayerCallbackStub() {
        // 使用弱引用持有 Activity，防止内存泄漏
        private val weakReference = WeakReference(playerActivity)

        override fun songChange(songBean: SongBean) {
            weakReference.get()?.let {
                it.runOnUiThread {
                    it.showSongInfo(songBean)
                }
            }
        }

        override fun songImageLoadDone(songImage: Bitmap) {
            weakReference.get()?.let {
                it.runOnUiThread {
                    it.showSongImage(songImage)
                }
            }
        }

        override fun startPrepare() {
            weakReference.get()?.let {
                it.runOnUiThread {
                    it.startPrepare()
                }
            }
        }

        override fun prepareDone() {
            weakReference.get()?.let {
                it.runOnUiThread {
                    it.prepareDone()
                }
            }
        }
    }

    /**
     * 带动画结束
     */
    override fun animFinish() {
        finish()
        overridePendingTransition(0, R.anim.translation_top_bottom_to)
    }
}