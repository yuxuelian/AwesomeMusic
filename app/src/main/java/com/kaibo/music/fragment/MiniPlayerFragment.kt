package com.kaibo.music.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.toast.ToastUtils
import com.kaibo.core.util.bindLifecycle
import com.kaibo.core.util.easyClick
import com.kaibo.core.util.singleAsync
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.activity.PlayerActivity
import com.kaibo.music.bean.SongBean
import com.kaibo.music.dialog.BeingPlayListDialog
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

    private var disposable: Disposable? = null

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
        mini_play_layout.easyClick(bindLifecycle()).subscribe {
            if (PlayManager.playSongQueue.isEmpty()) {
                ToastUtils.showWarning("播放队列为空")
            } else {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), playRotaImg, getString(R.string.transition_share_song_img))
                val intent = Intent(requireContext(), PlayerActivity::class.java)
                startActivity(intent, options.toBundle())
//                animStartActivity<PlayerActivity>()
            }
        }

        // 点击播放暂停按钮
        playOrPauseBtn.easyClick(bindLifecycle()).subscribe {
            singleAsync(bindLifecycle()) {
                PlayManager.togglePlayer()
            }
        }

        val beingPlayListDialog = BeingPlayListDialog()
        // 点击正在播放的歌曲列表
        playListBtn.easyClick(bindLifecycle()).subscribe {
            // 现实底部Dialog
            beingPlayListDialog.show(childFragmentManager)
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

}