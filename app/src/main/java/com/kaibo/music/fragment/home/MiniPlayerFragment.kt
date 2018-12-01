package com.kaibo.music.fragment.home

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet
import androidx.annotation.RequiresApi
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.toast.ToastUtils
import com.kaibo.core.util.easyClick
import com.kaibo.core.util.singleAsync
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.bean.SongBean
import com.kaibo.music.dialog.BeingPlayListDialog
import com.kaibo.music.fragment.RootFragment
import com.kaibo.music.fragment.player.PlayerFragment
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

    companion object {
        fun newInstance(arguments: Bundle = Bundle()): MiniPlayerFragment {
            return MiniPlayerFragment().apply {
                this.arguments = arguments
            }
        }
    }

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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private class DetailTransition : TransitionSet() {
        init {
            ordering = TransitionSet.ORDERING_TOGETHER
            addTransition(ChangeBounds()).addTransition(ChangeTransform()).addTransition(ChangeImageTransform())
        }
    }

    override fun getLayoutRes() = R.layout.fragment_mini_player

    override fun initViewCreated(savedInstanceState: Bundle?) {
        // 点击底部的播放条
        mini_play_layout.easyClick(bindLifecycle()).subscribe {
            if (PlayManager.playSongQueue.isEmpty()) {
                ToastUtils.showWarning("播放队列为空")
            } else {
                // 使用父Fragment来启动
                val rootFragment = (parentFragment as RootFragment)
                val playerFragment = PlayerFragment.newInstance()
                // TODO 5.0以上系统使用共享元素的方式启动
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//                // 退出Transition
//                rootFragment.exitTransition = Fade()
//                // 进入Transition
//                playerFragment.enterTransition = Fade()
//                // 共享返回共享元素
//                playerFragment.sharedElementReturnTransition = DetailTransition()
//                // 进入共享元素
//                playerFragment.sharedElementEnterTransition = DetailTransition()
//                // 25.1.0以下的support包,Material过渡动画只有在进栈时有,返回时没有;
//                // 25.1.0+的support包，SharedElement正常
//                rootFragment.extraTransaction().addSharedElement(playRotaImg, getString(R.string.transition_share_song_img)).start(playerFragment)
//            } else {
//                // 启动播放界面
//
//            }
                rootFragment.start(playerFragment)
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

        disposable = Observable
                .interval(100L, 100L, TimeUnit.MILLISECONDS)
                .toMainThread()
                .subscribe({
                    tickTask()
                }) {
                    it.printStackTrace()
                }
    }

    override fun onDestroyView() {
        disposable?.dispose()
        super.onDestroyView()
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