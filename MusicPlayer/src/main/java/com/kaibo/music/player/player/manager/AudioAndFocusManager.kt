package com.kaibo.music.player.player.manager

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.session.MediaSession
import android.os.Build
import com.kaibo.music.player.player.receiver.MediaButtonIntentReceiver
import com.orhanobut.logger.Logger

/**
 * 音频管理类
 * 主要用来管理音频焦点
 */

class AudioAndFocusManager(context: Context) {

    private val mAudioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /**
     * 音频焦点改变监听器
     */
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
        // TODO 发送音频焦点
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val mediaSession = MediaSession(context, "AudioAndFocusManager")
            val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
            mediaButtonIntent.component = ComponentName(context.packageName, MediaButtonIntentReceiver::class.java.name)
            val mPendingIntent = PendingIntent.getBroadcast(context, 0, mediaButtonIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            mediaSession.setMediaButtonReceiver(mPendingIntent)
        }
    }

    /**
     * 请求音频焦点
     */
    fun requestAudioFocus() {
        val requestRes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mAudioFocusRequest = AudioFocusRequest
                    .Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
            mAudioManager.requestAudioFocus(mAudioFocusRequest)
        } else {
            mAudioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        Logger.d("requestAudioFocus requestRes = $requestRes")
    }

    /**
     * 释放音频焦点
     */
    fun abandonAudioFocus() {
        val requestRes = mAudioManager.abandonAudioFocus(audioFocusChangeListener)
        Logger.d("abandonAudioFocus requestRes = $requestRes")
    }
}
