package com.kaibo.music.play

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.kaibo.core.bus.RxBus
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * @author kaibo
 * @date 2018/10/16 14:47
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class PlayerService : Service() {

    private var disposable: Disposable? = null

    internal val mediaPlayer by lazy {
        MediaPlayer()
    }

    private val testDataSource = "https://music.kaibo123.com/amobile.music.tc.qq.com/C400003OU9ul1LEU9T.m4a?guid=4715368380&vkey=F3DA36B2E856E4F87A02E2B55C27714B0DF3540E9E6CEBDB51337D852F2C9EDB17FAFE803BDFABD97FD19DE24BC2A8D0C68D2A9DCEE6A74A&uin=0&fromtag=999"

    override fun onBind(p0: Intent): IBinder {
        return PlayerStatusBinder(this)
    }

    override fun onCreate() {
        super.onCreate()
        initMediaPlayer()
        try {
            // 测试播放音乐
            playMusic(testDataSource)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initMediaPlayer() {
        // 准备完成监听
        mediaPlayer.setOnPreparedListener {
            RxBus.post(PlayDurationBean(it.duration))
        }

        // 缓存监听
        mediaPlayer.setOnBufferingUpdateListener { mediaPlayer, i ->

        }

        // 进度调整完成
        mediaPlayer.setOnSeekCompleteListener {
            Logger.d("setOnSeekCompleteListener")
        }

        // 发生错误
        mediaPlayer.setOnErrorListener { mediaPlayer, var2, var3 ->
            Logger.d("setOnErrorListener")
            true
        }

        mediaPlayer.setOnInfoListener { mediaPlayer, var1, var2 ->
            true
        }

        // 播放完成
        mediaPlayer.setOnCompletionListener {
            Logger.d("setOnCompletionListener")
        }

        // 初始化进度通知
        initNotifySeek()
    }

    private fun initNotifySeek() {
        disposable = Observable
                .interval(1000L, TimeUnit.MILLISECONDS)
                .subscribe({
                    // 正在播放才向外发送进度
                    if (mediaPlayer.isPlaying) {
                        // 向外发送播放进度
                        val seek = mediaPlayer.currentPosition
                        // 发送seek
                        RxBus.post(PlaySeekBean(seek))
                    }
                }) {
                    it.printStackTrace()
                }
    }

    private fun playMusic(dataSource: String) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        mediaPlayer.setDataSource(dataSource)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 接收到控制命令
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        // 停止播放 释放资源
        mediaPlayer.stop()
        mediaPlayer.release()
        // 释放心跳
        disposable?.dispose()
        super.onDestroy()
    }

}