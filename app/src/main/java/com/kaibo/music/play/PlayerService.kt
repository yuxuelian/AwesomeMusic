package com.kaibo.music.play

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.kaibo.core.bus.RxBus
import com.kaibo.core.util.toMainThread
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

/**
 * @author kaibo
 * @date 2018/10/16 14:47
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class PlayerService : Service() {

    private val disposables by lazy {
        CompositeDisposable()
    }

    internal val mediaPlayer by lazy {
        MediaPlayer()
    }

    // 当前正在播放的源
    private var playDataSource = ""

    override fun onBind(p0: Intent): IBinder {
        return PlayerStatusBinder(this)
    }

    override fun onCreate() {
        super.onCreate()
        initMediaPlayer()
        // 监听进度命令
        val seekDisposable = RxBus
                .toObservable<SeekCommand>()
                .subscribe {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.start()
                        // 播放状态发生改变
                        RxBus.post(PlayStatusChange(true))
                    }
                    mediaPlayer.seekTo(it.seek)
                }
        // 播放暂停命令监听
        val playDataDisposable = RxBus.toObservable<PlayCommand>()
                .subscribe {
                    if (playDataSource != it.dataSource) {
                        // 记录下播放源
                        playDataSource = it.dataSource
                        // 设置播放源
                        try {
                            // 测试播放音乐
                            playNewDataSource(it.dataSource)
                            // 把duration发送出去
                            RxBus.post(PlayDurationBean(mediaPlayer.duration))
                        } catch (e: Exception) {
                            // TODO 设置播放源出错  需要发送错误信息出去
                            e.printStackTrace()

                            // 播放暂停
                            RxBus.post(PlayStatusChange(false))
                        }
                    } else {
                        if (it.isPlay) {
                            // 没有播放 就启动播放
                            if (!mediaPlayer.isPlaying) {
                                mediaPlayer.start()
                                // 把duration发送出去
                                RxBus.post(PlayDurationBean(mediaPlayer.duration))
                            }
                        } else {
                            // 正在播放 就暂停播放
                            if (mediaPlayer.isPlaying) {
                                mediaPlayer.pause()
                            }
                        }
                    }
                }
        disposables.addAll(seekDisposable, playDataDisposable)
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
            // 发送播放完成的事件出去
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
        val disposable = Observable
                .interval(1000L, TimeUnit.MILLISECONDS)
                .toMainThread()
                .subscribe({
                    // 正在播放才向外发送进度
                    if (mediaPlayer.isPlaying) {
                        // 向外发送播放进度
                        val seek = mediaPlayer.currentPosition
                        // 发送seek
                        RxBus.post(PlayProgressBean(seek))
                    }
                }) {
                    it.printStackTrace()
                }
        disposables.add(disposable)
    }

    private fun playNewDataSource(dataSource: String) {
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
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
        super.onDestroy()
    }

}