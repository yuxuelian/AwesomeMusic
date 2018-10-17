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

    private val mediaPlayer by lazy {
        MediaPlayer()
    }

    // 记录播放器的播放状态
    private val dataSourceStatus = DataSourceStatus()
    private val playingStatus = PlayingStatus()
    private val durationStatus = DurationStatus()
    private val seekStatus = SeekStatus()

    override fun onBind(p0: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initMediaPlayer()
        // 播放暂停命令监听
        val playDataDisposable = RxBus.toObservable<PlayerCommand>()
                .subscribe {
                    when (it) {
                        is DataSourceCommand -> {
                            dataSourceCommand(it)
                        }
                        is SeekCommand -> {
                            if (!mediaPlayer.isPlaying) {
                                mediaPlayer.start()
                                // 播放状态发生改变
                            }
                            mediaPlayer.seekTo(it.seek)
                        }
                    }
                }
        disposables.add(playDataDisposable)
    }

    private fun dataSourceCommand(it: DataSourceCommand) {
        if (dataSourceStatus.songBean == null) {
            dataSourceStatus.songBean = it.songBean
        } else {
            if (dataSourceStatus.songBean!!.url != it.songBean.url) {
                dataSourceStatus.songBean = it.songBean
            } else {
                // 同一个播放源,然后判断是否正在播放,没有播放则启动播放
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
                return
            }
        }
        // 设置播放源
        try {
            // 测试播放音乐
            playNewDataSource(it.songBean.url)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initMediaPlayer() {
        // 准备完成监听
        mediaPlayer.setOnPreparedListener {
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
                        seekStatus.seek = mediaPlayer.currentPosition
                    }
                    // 发送seek
                    RxBus.post(seekStatus)
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