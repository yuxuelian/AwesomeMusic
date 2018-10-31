package com.kaibo.music.fragment

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.util.animInStartActivity
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.activity.PlayerActivity
import com.kaibo.music.bean.SongBean
import com.kaibo.music.player.manager.PlayManager
import com.kaibo.music.utils.AnimatorUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_mini_player.*
import java.util.concurrent.TimeUnit

/**
 * @author kaibo
 * @date 2018/10/31 14:11
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class MiniPlayerFragment : BaseFragment() {

    private var currentSongBean: SongBean? = null
        set(value) {
            if (value != null && value != field) {
                field = value
                // 修改歌曲名
                songName.text = value.songname
                singerName.text = value.singername
                // 修改显示的歌曲图片
                Observable
                        .create<Bitmap> {
                            try {
                                it.onNext(GlideApp.with(this).asBitmap().load(value.image).submit().get())
                                it.onComplete()
                            } catch (e: Throwable) {
                                it.onError(e)
                            }
                        }
                        .subscribeOn(Schedulers.io())
                        .toMainThread()
                        .`as`(bindLifecycle())
                        .subscribe({
                            // 设置到旋转的ImageView
                            playRotaImg.setImageBitmap(it)
                        }) {

                        }
            }
        }

    private var isPlaying = false
        set(value) {
            if (value != field) {
                // 获取播放状态并记录状态
                field = value
                if (value) {
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

            // 播放状态发生改变
            playOrPauseBtn.setImageResource(if (value) {
                R.drawable.big_pause
            } else {
                R.drawable.big_play
            })
        }

    /**
     * 旋转动画
     */
    private val rotateAnimator by lazy {
        AnimatorUtils.getRotateAnimator(playRotaImg)
    }

    override fun getLayoutRes() = R.layout.fragment_mini_player

    override fun initViewCreated(savedInstanceState: Bundle?) {
        // 点击底部的播放条
        mini_play_layout.clicks().`as`(bindLifecycle()).subscribe {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val intent = Intent(activity, PlayerActivity::class.java)
                activity?.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity, playRotaImg, getString(R.string.transition_share_song_img)).toBundle())
            } else {
                activity?.animInStartActivity<PlayerActivity>()
            }
        }

        // 点击播放暂停按钮
        playOrPauseBtn.clicks().`as`(bindLifecycle()).subscribe {
            PlayManager.togglePlayer()
            playOrPauseBtn.setImageResource(if (PlayManager.isPlaying) {
                R.drawable.big_pause
            } else {
                R.drawable.big_play
            })
        }
    }

    /**
     * 每隔200ms执行一次这个方法
     */
    private fun tickTask() {
        // 获取播放的歌曲
        currentSongBean = PlayManager.playSong
        // 获取播放状态
        isPlaying = PlayManager.isPlaying
        // 歌曲进度修改
        miniProgressBar.max = PlayManager.duration
        miniProgressBar.progress = PlayManager.currentPosition
    }

    private var disposable: Disposable? = null

    override fun onResume() {
        super.onResume()
        disposable = Observable
                .interval(100L, 100L, TimeUnit.MILLISECONDS)
                .toMainThread()
                .subscribe({
                    tickTask()
                }) {
                    it.printStackTrace()
                }
    }

    override fun onPause() {
        disposable?.dispose()
        super.onPause()
    }

}