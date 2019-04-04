package com.kaibo.music.player.player

import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import com.kaibo.music.player.player.service.MusicPlayerService

class MediaPlayerEngine(
        private val musicPlayerService: MusicPlayerService
) :
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener {

    private var mMediaPlayer = MediaPlayer()

    init {
        // 无需显示的为其解锁(release的时候会自动解锁)
        mMediaPlayer.setWakeMode(musicPlayerService, PowerManager.PARTIAL_WAKE_LOCK)
    }

    /**
     * 是否已经初始化
     */
    var isInitialized = false
        private set

    /**
     * 是否准备完成
     */
    var isPrepared = false
        private set

    /**
     * getDuration 只能在prepared之后才能调用，不然会报-38错误
     *
     * @return
     */
    val duration: Int
        get() = if (isPrepared) {
            mMediaPlayer.duration
        } else {
            0
        }

    val currentPosition: Int
        get() {
            return try {
                mMediaPlayer.currentPosition
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                0
            }
        }

    val audioSessionId: Int
        get() = mMediaPlayer.audioSessionId

    /**
     * 设置播放源
     *
     * @param path
     */
    fun setDataSource(path: String) {
        // 返回是否初始化成功
        isInitialized = setDataSourceImpl(path)
    }

    private fun setDataSourceImpl(path: String): Boolean {
        return try {
            // 如果正在播放,则停止播放
            if (mMediaPlayer.isPlaying) {
                mMediaPlayer.stop()
            }
            isPrepared = false
            // 重置一下播放器,同步状态
            mMediaPlayer.reset()
            if (path.startsWith("content://")) {
                mMediaPlayer.setDataSource(musicPlayerService, Uri.parse(path))
            } else {
                mMediaPlayer.setDataSource(path)
            }
            mMediaPlayer.prepareAsync()
            mMediaPlayer.setOnPreparedListener(this)
            mMediaPlayer.setOnBufferingUpdateListener(this)
            mMediaPlayer.setOnErrorListener(this)
            mMediaPlayer.setOnCompletionListener(this)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 启动播放
     */
    fun start() {
        mMediaPlayer.start()
    }

    /**
     * 停止播放
     */
    fun stop() {
        try {
            mMediaPlayer.reset()
            isInitialized = false
            isPrepared = false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    /**
     * 释放播放器
     */
    fun release() {
        mMediaPlayer.release()
    }

    /**
     * 暂停播放
     */
    fun pause() {
        mMediaPlayer.pause()
    }

    fun seek(whereto: Int) {
        mMediaPlayer.seekTo(whereto)
    }

    /**
     * 音量控制
     *
     * @param vol
     */
    fun setVolume(vol: Float) {
        mMediaPlayer.setVolume(vol, vol)
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        when (what) {
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> {
                isInitialized = false
                // 释放上一个播放器的资源
                mMediaPlayer.release()
                // 重新创建一个新的播放器
                mMediaPlayer = MediaPlayer()
                // 屏幕长亮
                mMediaPlayer.setWakeMode(musicPlayerService, PowerManager.PARTIAL_WAKE_LOCK)
                // TODO 将错误信息发送出去
                return true
            }
            else -> {
            }
        }
        return true
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        // 播放完成  切换下一首歌
        musicPlayerService.next()
    }

    override fun onBufferingUpdate(mediaPlayer: MediaPlayer, percent: Int) {

    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        // 准备完成
        mediaPlayer.start()
        // 准备结束标记置为true
        isPrepared = true
        // 准备完成,回调
        musicPlayerService.onPrepared()
    }
}
