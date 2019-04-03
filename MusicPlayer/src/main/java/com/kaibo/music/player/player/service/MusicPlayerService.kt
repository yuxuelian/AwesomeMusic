package com.kaibo.music.player.player.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.IBinder
import android.os.RemoteCallbackList
import android.support.v4.media.session.PlaybackStateCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.kaibo.music.player.IPlayerStateCallback
import com.kaibo.music.player.R
import com.kaibo.music.player.bean.LyricRowBean
import com.kaibo.music.player.bean.SongBean
import com.kaibo.music.player.player.MediaPlayerWrap
import com.kaibo.music.player.player.listener.ServicePhoneStateListener
import com.kaibo.music.player.player.manager.AudioAndFocusManager
import com.kaibo.music.player.player.manager.MediaSessionManager
import com.kaibo.music.player.player.receiver.HeadsetPlugInReceiver
import com.kaibo.music.player.player.receiver.HeadsetReceiver
import com.kaibo.music.player.player.receiver.PlayerCommandReceiver
import com.kaibo.music.player.utils.loadBitmap
import com.kaibo.music.player.utils.pref
import com.orhanobut.logger.Logger
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * 音乐播放后台服务
 */
class MusicPlayerService : Service() {

    companion object {
        /**
         * 播放模式 0：顺序播放，1：单曲循环，2：随机播放
         * 默认播放模式是顺序播放
         */
        private const val PLAY_MODE_LOOP = 0
        private const val PLAY_MODE_REPEAT = 1
        private const val PLAY_MODE_RANDOM = 2
    }

    /**
     * 播放模式
     */
    private var mPlayMode by pref(this, PLAY_MODE_LOOP)

    /**
     * 播放队列
     */
    private val mPlayQueue: MutableList<SongBean> = ArrayList()

    private val remoteCallbackList = RemoteCallbackList<IPlayerStateCallback>()

    /**
     * 记录播放位置
     */
    private var mPlayingQueueIndex = -1

    /**
     * 是否正在播放
     */
    private var isPlaying = false

    private lateinit var mHeadsetReceiver: HeadsetReceiver
    private lateinit var mServiceReceiver: PlayerCommandReceiver
    private lateinit var mHeadsetPlugInReceiver: HeadsetPlugInReceiver

    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var mAudioAndFocusManager: AudioAndFocusManager

    /**
     * 进程间通信,接口实现
     */
    private lateinit var iMusicPlayer: IMusicPlayerStub

    /**
     * 歌词
     */
    private var lyricRowBeans: List<LyricRowBean>? = null

    /**
     * 封装后的播放器
     */
    private lateinit var mMediaPlayer: MediaPlayerWrap

    // 是否显示桌面歌词
    private var isShowDesktopLyric by pref(this, false)

    private lateinit var mRemoteView: RemoteViews
    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mNotificationBuilder: NotificationCompat.Builder

    // 获取下一首位置
    private val nextPosition: Int
        get() {
            if (mPlayQueue.isEmpty()) {
                return -1
            }

            if (mPlayQueue.size == 1) {
                return 0
            }
            if (mPlayMode == MusicPlayerService.PLAY_MODE_REPEAT) {
                return if (mPlayingQueueIndex < 0) {
                    0
                } else {
                    mPlayingQueueIndex
                }
            } else if (mPlayMode == MusicPlayerService.PLAY_MODE_RANDOM) {
                val random = Random()
                var randomPosition = mPlayingQueueIndex
                while (randomPosition == mPlayingQueueIndex) {
                    randomPosition = random.nextInt(mPlayQueue.size)
                }
                return randomPosition
            } else {
                if (mPlayingQueueIndex == mPlayQueue.size - 1) {
                    return 0
                } else if (mPlayingQueueIndex < mPlayQueue.size - 1) {
                    return mPlayingQueueIndex + 1
                }
            }
            return mPlayingQueueIndex
        }

    // 获取上一曲位置
    private val previousPosition: Int
        get() {
            if (mPlayQueue.isEmpty()) {
                return -1
            }
            if (mPlayQueue.size == 1) {
                return 0
            }
            if (mPlayMode == MusicPlayerService.PLAY_MODE_REPEAT) {
                if (mPlayingQueueIndex < 0) {
                    return 0
                }
            } else if (mPlayMode == MusicPlayerService.PLAY_MODE_RANDOM) {
                val random = Random()
                var randomPosition = mPlayingQueueIndex
                while (randomPosition == mPlayingQueueIndex) {
                    randomPosition = random.nextInt(mPlayQueue.size)
                }
                return randomPosition
            } else {
                return when {
                    mPlayingQueueIndex == 0 -> mPlayQueue.size - 1
                    mPlayingQueueIndex > 0 -> mPlayingQueueIndex - 1
                    else -> 0
                }
            }
            return mPlayingQueueIndex
        }

    // 用于标记当前正在播放的歌曲信息
    private var songBean: SongBean? = null

    override fun onCreate() {
        super.onCreate()
        // --------------------------初始化音乐播放服务------------------------------------------
        iMusicPlayer = IMusicPlayerStub(this)
        mMediaPlayer = MediaPlayerWrap(this, iMusicPlayer)

        // 初始化媒体会话管理器和音频焦点管理器
        mediaSessionManager = MediaSessionManager(iMusicPlayer, this)
        mAudioAndFocusManager = AudioAndFocusManager(this)

        // -------------------------------初始化电话监听服务---------------------------------------
        // 获取电话通讯服务
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        // 创建一个监听对象，监听电话状态改变事件
        telephonyManager.listen(ServicePhoneStateListener(iMusicPlayer), PhoneStateListener.LISTEN_CALL_STATE)

        // ----------------------------------初始化广播-------------------------------------------
        mServiceReceiver = PlayerCommandReceiver(iMusicPlayer)
        mHeadsetReceiver = HeadsetReceiver(iMusicPlayer)
        mHeadsetPlugInReceiver = HeadsetPlugInReceiver()
        // 过滤器
        val intentFilter = IntentFilter()
//        intentFilter.addAction(Constants.ACTION_NEXT)
//        intentFilter.addAction(Constants.ACTION_PREV)
//        intentFilter.addAction(Constants.SHUTDOWN)
//        intentFilter.addAction(Constants.ACTION_PLAY_PAUSE)
        //注册广播
        registerReceiver(mServiceReceiver, intentFilter)
        registerReceiver(mHeadsetReceiver, intentFilter)
        registerReceiver(mHeadsetPlugInReceiver, intentFilter)

        //初始化通知
        initNotify()
        // 显示一次通知
        updateNotification()
    }

    /**
     * 初始化通知栏
     */
    private fun initNotify() {
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationBuilder = NotificationCompat.Builder(this, initNotifyChannel())
                .setSmallIcon(R.drawable.ic_icon)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
        // 小布局
        mRemoteView = RemoteViews(packageName, R.layout.player_notification)
        // 设置RemoteViews
        // 播放暂停
        mRemoteView.setOnClickPendingIntent(R.id.notificationPlayPause, createPendingIntent(Constants.ACTION_PLAY_PAUSE))
        // 关闭
        mRemoteView.setOnClickPendingIntent(R.id.notificationStop, createPendingIntent(Constants.ACTION_CLOSE))
        // 上一曲
        mRemoteView.setOnClickPendingIntent(R.id.notificationFRewind, createPendingIntent(Constants.ACTION_PREV))
        // 下一曲
        mRemoteView.setOnClickPendingIntent(R.id.notificationFForward, createPendingIntent(Constants.ACTION_NEXT))
        // 词
        mRemoteView.setOnClickPendingIntent(R.id.notificationLyric, createPendingIntent(Constants.ACTION_LYRIC))
        // 播放模式
        mRemoteView.setOnClickPendingIntent(R.id.notificationRepeat, createPendingIntent(Constants.ACTION_REPEAT))
        // 设置布局
        mNotificationBuilder.setCustomContentView(mRemoteView)
    }

    private fun initNotifyChannel(): String {
        // 通知渠道的id
        val id = "music_player"
        // Android 8.0需要创建这个通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(NotificationChannel(id, "music_player", NotificationManager.IMPORTANCE_LOW))
        }
        return id
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder = iMusicPlayer

    /**
     * 播放当前歌曲
     * 调用这个方法之前需要修改 mPlayingQueueIndex  否则调用无效
     */
    private fun playCurrentAndNext() {
        if (mPlayingQueueIndex >= mPlayQueue.size || mPlayingQueueIndex < 0) {
            return
        }
        val currentSong = mPlayQueue[mPlayingQueueIndex]
        val url = currentSong.url
        Logger.d("播放地址: url = $url")
        // 设置播放源
        mMediaPlayer.setDataSource(url)
        // 更新媒体状态
        mediaSessionManager.updateMetaData(songBean)
        // 请求音频焦点
        mAudioAndFocusManager.requestAudioFocus()
        loadLyric(currentSong)
        isPlaying = true
        songBean = currentSong
        notifyChange()
        updateNotification()
        val intent = Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mMediaPlayer.audioSessionId)
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        sendBroadcast(intent)
    }

    private fun loadLyric(songBean: SongBean) {
        Logger.d("加载歌词  songBean = $songBean")
    }

    private fun savePlayQueue() {
        // TODO 保存播放队列
    }

    private fun notifyChange() {

    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(action)
        intent.component = ComponentName(this, MusicPlayerService::class.java)
        return PendingIntent.getService(this, 0, intent, 0)
    }

    /**
     * 更新通知栏
     */
    private fun updateNotification() {
        // 切歌时所有状态都要重新设置一次
        mRemoteView.setTextViewText(R.id.notificationSongName, songBean?.songname ?: "歌曲名")
        mRemoteView.setTextViewText(R.id.notificationArtist, songBean?.singername ?: "歌手名")
        // 更新播放按钮状态
        val playButtonResId = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        mRemoteView.setImageViewResource(R.id.notificationPlayPause, playButtonResId)
        // 更新歌词状态
        val lyricResId = if (isShowDesktopLyric) R.drawable.ic_lyric_show else R.drawable.ic_lyric_hide
        mRemoteView.setImageViewResource(R.id.notificationLyric, lyricResId)
        // 播放模式
        when (mPlayMode) {
            MusicPlayerService.PLAY_MODE_REPEAT -> mRemoteView.setImageViewResource(R.id.notificationRepeat, R.drawable.ic_repeat_one)
            MusicPlayerService.PLAY_MODE_RANDOM -> mRemoteView.setImageViewResource(R.id.notificationRepeat, R.drawable.ic_repeat_random)
            else -> mRemoteView.setImageViewResource(R.id.notificationRepeat, R.drawable.ic_repeat)
        }
        // 显示到通知栏
        startForeground(Constants.NOTIFICATION_ID, mNotificationBuilder.build())

        // 加载图片
        songBean?.image?.let {
            val disposable: Disposable = loadBitmap(it).subscribe({
                // 图片加载完成
                mRemoteView.setImageViewBitmap(R.id.notificationCover, it)
                // 显示到通知栏
                startForeground(Constants.NOTIFICATION_ID, mNotificationBuilder.build())
            }) {
                it.printStackTrace()
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    fun togglePlayer() {
        if (isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun pause() {
        // mMediaPlayer 已经初始化了  并且当前正在播放  才进行暂停操作
        if (mMediaPlayer.isInitialized && isPlaying) {
            isPlaying = false
            // 通知播放状态
            notifyChange()
            // 更新状态栏
            updateNotification()
            // 调用播放引擎的暂停播放方法
            mMediaPlayer.pause()
        }
    }

    fun play() {
        if (mMediaPlayer.isInitialized) {
            // 启动播放
            mMediaPlayer.start()
            // 修改播放状态
            isPlaying = true
            // 全局发送播放状态改变的广播
            notifyChange()
            // 请求音频焦点
            mAudioAndFocusManager.requestAudioFocus()
            // 更新通知栏
            updateNotification()
        } else {
            // 获取下一首歌曲应该播放的位置
            mPlayingQueueIndex = nextPosition
            playCurrentAndNext()
        }
    }

    fun stop() {
        // 修改播放的状态
        isPlaying = false
        if (mMediaPlayer.isInitialized) {
            // 停止播放引擎中正在播放的歌曲
            mMediaPlayer.stop()
        }
    }

    fun prev() {
        // 获取上一首歌位置
        mPlayingQueueIndex = previousPosition
        // 停止当前正在播放的歌曲
        stop()
        // 切换歌曲
        playCurrentAndNext()
    }

    fun next() {
        // 获取下一首歌的播放位置
        mPlayingQueueIndex = nextPosition
        // 停止当前正在播放的歌曲
        stop()
        // 切换歌曲
        playCurrentAndNext()
    }

    fun getDuration(): Int {
        return mMediaPlayer.duration
    }

    fun seekTo(pos: Int) {
        if (mMediaPlayer.isInitialized) {
            mMediaPlayer.seek(pos)
            if (!isPlaying) {
                play()
            }
        }
    }

    fun getCurrentPosition(): Int {
        return mMediaPlayer.currentPosition
    }

    fun setPlayPosition(pos: Int) {
        mPlayingQueueIndex = if (pos >= mPlayQueue.size || pos == -1) {
            nextPosition
        } else {
            pos
        }
        if (mPlayingQueueIndex == -1) {
            return
        }
        playCurrentAndNext()
    }

    fun getPlayPosition(): Int {
        return if (mPlayingQueueIndex >= 0) {
            mPlayingQueueIndex
        } else {
            0
        }
    }

    fun setPlaySong(songBean: SongBean) {
        var hasSong = false
        // 去播放队列中寻找是否已经存在当前正在播放的歌曲了
        var hasPosition = 0
        while (hasPosition < mPlayQueue.size) {
            if (songBean == mPlayQueue[hasPosition]) {
                hasSong = true
                break
            }
            hasPosition++
        }
        if (hasSong) {
            // 播放队列中已经存在需要播放的歌曲了
            mPlayingQueueIndex = hasPosition
        } else {
            if (mPlayingQueueIndex == -1 || mPlayQueue.size == 0) {
                // 播放的是第一首歌曲
                mPlayQueue.add(songBean)
                mPlayingQueueIndex = 0
            } else if (mPlayingQueueIndex < mPlayQueue.size) {
                // 直接添加到 mPlayingQueueIndex 地方去
                mPlayQueue.add(mPlayingQueueIndex, songBean)
            }
        }
        // 执行播放操作
        playCurrentAndNext()
    }

    fun getPlaySong(): SongBean? {
        return songBean
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun isPrepared(): Boolean {
        return mMediaPlayer.isPrepared
    }

    fun getPlayQueue(): List<SongBean> {
        return mPlayQueue
    }

    fun setPlayQueue(playQueue: List<SongBean>) {
        songBean = null
        isPlaying = false
        mPlayQueue.clear()
        mPlayQueue.addAll(playQueue)
        savePlayQueue()

        mPlayingQueueIndex = 0
        playCurrentAndNext()
    }

    fun removeAt(position: Int) {
        if (position == mPlayingQueueIndex) {
            mPlayQueue.removeAt(position)
            if (mPlayQueue.size == 0) {
                clearPlayQueue()
            } else {
                mPlayingQueueIndex = position
            }
        } else if (position > mPlayingQueueIndex) {
            mPlayQueue.removeAt(position)
        } else {
            mPlayQueue.removeAt(position)
            mPlayingQueueIndex--
        }
        notifyChange()
    }

    fun remove(songBean: SongBean) {
        val position = mPlayQueue.indexOf(songBean)
        removeAt(position)
    }

    fun showDesktopLyric() {
        isShowDesktopLyric = !isShowDesktopLyric
        updateNotification()
        if (isShowDesktopLyric) {
            // TODO 显示桌面歌词

        } else {
            // TODO 关闭桌面歌词
        }
    }

    fun clearPlayQueue() {
        songBean = null
        isPlaying = false
        mPlayingQueueIndex = -1
        stop()
        mPlayQueue.clear()
        savePlayQueue()
        notifyChange()
    }

    fun updatePlayMode(): Int {
        mPlayMode = (mPlayMode + 1) % 3
        // 修改通知栏的显示情况
        updateNotification()
        return mPlayMode
    }

    fun getLyricRowBeans(): List<LyricRowBean>? {
        return lyricRowBeans
    }

    fun exit() {
        stopSelf()
    }

    fun registerCallback(callback: IPlayerStateCallback) {
        remoteCallbackList.register(callback)
    }

    fun unregisterCallback(callback: IPlayerStateCallback) {
        remoteCallbackList.unregister(callback)
    }

    // ----------------------------------------------------------------------------------------------------------------
    private fun execDestroy() {
        // 关闭audio媒体中心
        val audioEffectsIntent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mMediaPlayer.audioSessionId)
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        sendBroadcast(audioEffectsIntent)
        // 保存播放队列
        savePlayQueue()
        //释放mPlayer
        isPlaying = false
        mMediaPlayer.stop()
        mMediaPlayer.release()
        // 取消桌面歌词显示
        isShowDesktopLyric = false
        showDesktopLyric()
        // 释放音频焦点
        mAudioAndFocusManager.abandonAudioFocus()
        // 取消通知栏
        stopForeground(true)
        // 注销广播
        unregisterReceiver(mServiceReceiver)
        unregisterReceiver(mHeadsetReceiver)
        unregisterReceiver(mHeadsetPlugInReceiver)
        // 注销远程回调
        remoteCallbackList.kill()
    }

    override fun onDestroy() {
        execDestroy()
        super.onDestroy()
    }
}
