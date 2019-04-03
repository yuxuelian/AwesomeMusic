package com.kaibo.music.player.player.manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.kaibo.music.player.IMusicPlayer
import com.kaibo.music.player.bean.SongBean
import com.orhanobut.logger.Logger

class MediaSessionManager(private val iMusicPlayer: IMusicPlayer, context: Context) {

    companion object {
        /**
         * 指定可以接收的来自锁屏页面的按键信息
         */
        private const val MEDIA_SESSION_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_SEEK_TO)
    }

    private val mMediaSession: MediaSessionCompat = MediaSessionCompat(context, "MediaSessionManager")

    /**
     * API 21 以上 耳机多媒体按钮监听 MediaSessionCompat.Callback
     */
    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            iMusicPlayer.togglePlayer()
        }

        override fun onPause() {
            iMusicPlayer.togglePlayer()
        }

        override fun onSkipToNext() {
            iMusicPlayer.next()
        }

        override fun onSkipToPrevious() {
            iMusicPlayer.prev()
        }

        override fun onStop() {
            iMusicPlayer.togglePlayer()
        }

        override fun onSeekTo(pos: Long) {
            iMusicPlayer.seekTo(pos.toInt())
        }
    }

    private val mHandler =
            @SuppressLint("HandlerLeak")
            object : Handler() {
                override fun handleMessage(msg: Message) {
                    Logger.d("MediaSessionManager  handleMessage")
                }
            }

    init {
        //        第二个参数 tag: 这个是用于调试用的,随便填写即可
        //指明支持的按键信息类型
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mMediaSession.setCallback(callback, mHandler)
        mMediaSession.isActive = true
    }

    private val currentPosition: Long
        get() = iMusicPlayer.currentPosition.toLong()

    /**
     * 是否在播放
     *
     * @return
     */
    private val isPlaying: Boolean
        get() = iMusicPlayer.isPlaying

    private val duration: Int
        get() = iMusicPlayer.duration

    private val count: Int
        get() = iMusicPlayer.playQueue.size

    /**
     * 更新播放状态， 播放／暂停／拖动进度条时调用
     */
    fun updatePlaybackState() {
        val state = if (isPlaying) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }
        mMediaSession.setPlaybackState(
                PlaybackStateCompat
                        .Builder()
                        .setActions(MEDIA_SESSION_ACTIONS)
                        .setState(state, currentPosition, 1f)
                        .build()
        )
    }

    /**
     * 更新正在播放的音乐信息，切换歌曲时调用
     */
    fun updateMetaData(songInfo: SongBean?) {
        if (songInfo == null) {
            mMediaSession.setMetadata(null)
            return
        }
        val builder = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songInfo.songname)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songInfo.singername)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, songInfo.singername)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration.toLong())
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, count.toLong())
        //        Disposable disposable = DownLoadManager.INSTANCE.downImage(songInfo.getImage()).subscribe(bitmap -> {
        //            // 图片加载完成
        //            metaDta.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
        //            mMediaSession.setMetadata(metaDta.build());
        //        }, Throwable::printStackTrace);

        mMediaSession.setMetadata(builder.build())
    }

    /**
     * 释放MediaSession，退出播放器时调用
     */
    fun release() {
        mMediaSession.setCallback(null)
        mMediaSession.isActive = false
        mMediaSession.release()
    }
}
