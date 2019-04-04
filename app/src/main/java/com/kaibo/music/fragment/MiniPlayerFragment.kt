package com.kaibo.music.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.toast.ToastUtils
import com.kaibo.core.util.bindLifecycle
import com.kaibo.core.util.easyClick
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.activity.PlayerActivity
import com.kaibo.music.callback.BasePlayerCallbackStub
import com.kaibo.music.dialog.BeingPlayListDialog
import com.kaibo.music.player.PlayerController
import com.kaibo.music.player.bean.SongBean
import com.kaibo.music.player.utils.AnimatorUtils
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_mini_player.*
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit


/**
 * @author kaibo
 * @date 2018/10/31 14:11
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class MiniPlayerFragment : BaseFragment() {

    private val beingPlayListDialog by lazy {
        BeingPlayListDialog()
    }

    /**
     * 旋转动画
     */
    private val rotateAnimator by lazy {
        AnimatorUtils.getRotateAnimator(playRotaImg)
    }

    private val playerCallbackStub = PlayerCallbackStub(this)

    override fun getLayoutRes() = R.layout.fragment_mini_player

    override fun initViewCreated(savedInstanceState: Bundle?) {
        // 点击底部的播放条
        mini_play_layout.easyClick(bindLifecycle()).subscribe {
            if (PlayerController.getPlayQueue()?.isEmpty() == true) {
                ToastUtils.showWarning("播放队列为空")
            } else {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), playRotaImg, getString(R.string.transition_share_song_img))
                val intent = Intent(requireContext(), PlayerActivity::class.java)
                startActivity(intent, options.toBundle())
            }
        }

        // 点击播放暂停按钮
        playOrPauseBtn.easyClick(bindLifecycle()).subscribe {
            PlayerController.togglePlayer()
        }

        // 点击正在播放的歌曲列表
        playListBtn.easyClick(bindLifecycle()).subscribe {
            // 现实底部Dialog
            beingPlayListDialog.show(childFragmentManager)
        }
    }

    override fun onResume() {
        super.onResume()
        PlayerController.registerCallback(playerCallbackStub)

        // 主动获取一次歌曲信息
        PlayerController.getPlaySong()?.let(::showSongInfo)
        PlayerController.getSongImage()?.let(::showSongImage)

        // 刷新一次进度条
        miniProgressBar.max = PlayerController.getDuration()
        miniProgressBar.progress = PlayerController.getCurrentPosition()

        if (PlayerController.isPrepared()) {
            prepareDone()
        } else {
            startPrepare()
        }
    }

    override fun onPause() {
        PlayerController.unregisterCallback(playerCallbackStub)
        super.onPause()
    }

    private fun showSongInfo(songBean: SongBean) {
        songName.text = songBean.songname
        singerName.text = songBean.singername
    }

    private fun showSongImage(songImage: Bitmap) {
        playRotaImg.rotation = 0f
        playRotaImg.setImageBitmap(songImage)
    }

    private fun startPrepare() {
        // 显示加载条
        playOrPauseBtn.visibility = View.GONE
        prepareProgress.visibility = View.VISIBLE
    }

    private fun prepareDone() {
        // 停止加载条
        playOrPauseBtn.visibility = View.VISIBLE
        prepareProgress.visibility = View.GONE
        // 获取播放总时长
        miniProgressBar.max = PlayerController.getDuration()
        // 这个心跳在界面 pause 的时候会自动停止
        Observable.interval(1000L, TimeUnit.MILLISECONDS).toMainThread()
                .`as`(bindLifecycle(Lifecycle.Event.ON_PAUSE)).subscribe({
                    miniProgressBar.progress = PlayerController.getCurrentPosition()
                }) {
                    it.printStackTrace()
                }
    }

    private class PlayerCallbackStub(miniPlayerFragment: MiniPlayerFragment) : BasePlayerCallbackStub() {
        private val weakReference = WeakReference(miniPlayerFragment)

        override fun songChange(songBean: SongBean) {
            weakReference.get()?.let { fragment ->
                fragment.activity?.runOnUiThread {
                    fragment.showSongInfo(songBean)
                }
            }
        }

        override fun songImageLoadDone(songImage: Bitmap) {
            weakReference.get()?.let { fragment ->
                fragment.activity?.runOnUiThread {
                    fragment.showSongImage(songImage)
                }
            }
        }

        override fun startPrepare() {
            weakReference.get()?.let { fragment ->
                fragment.activity?.runOnUiThread {
                    fragment.startPrepare()
                }
            }
        }

        override fun prepareDone() {
            weakReference.get()?.let { fragment ->
                fragment.activity?.runOnUiThread {
                    fragment.prepareDone()
                }
            }
        }
    }
}