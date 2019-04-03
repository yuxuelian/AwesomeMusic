package com.kaibo.music.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.kaibo.music.player.bean.LyricRowBean
import com.kaibo.music.player.bean.SongBean
import com.kaibo.music.player.player.service.MusicPlayerService

/**
 * 对外提供的播放控制
 */
object PlayerController {

    private var iMusicPlayer: IMusicPlayer? = null

    fun togglePlayer() {
        iMusicPlayer?.togglePlayer()
    }

    fun pause() {
        iMusicPlayer?.pause()
    }

    fun play() {
        iMusicPlayer?.play()
    }

    fun stop() {
        iMusicPlayer?.stop()
    }

    fun prev() {
        iMusicPlayer?.prev()
    }

    fun next() {
        iMusicPlayer?.next()
    }

    fun getDuration(): Int {
        return iMusicPlayer?.getDuration() ?: 0
    }

    fun seekTo(pos: Int) {
        iMusicPlayer?.seekTo(pos)
    }

    fun getCurrentPosition(): Int {
        return iMusicPlayer?.getCurrentPosition() ?: 0
    }

    fun setPlayPosition(pos: Int) {
        iMusicPlayer?.setPlayPosition(pos)
    }

    fun getPlayPosition(): Int {
        return iMusicPlayer?.getPlayPosition() ?: -1
    }

    fun setPlaySong(songBean: SongBean) {
        iMusicPlayer?.setPlaySong(songBean)
    }

    fun getPlaySong(): SongBean? {
        return iMusicPlayer?.getPlaySong()
    }

    fun isPlaying(): Boolean {
        return iMusicPlayer?.isPlaying() ?: false
    }

    fun isPrepared(): Boolean {
        return iMusicPlayer?.isPrepared() ?: false
    }

    fun getPlayQueue(): List<SongBean>? {
        return iMusicPlayer?.getPlayQueue()
    }

    fun setPlayQueue(playQueue: List<SongBean>) {
        iMusicPlayer?.setPlayQueue(playQueue)
    }

    fun removeAt(position: Int) {
        iMusicPlayer?.removeAt(position)
    }

    fun remove(songBean: SongBean) {
        iMusicPlayer?.remove(songBean)
    }

    fun showDesktopLyric() {
        iMusicPlayer?.showDesktopLyric()
    }

    fun clearPlayQueue() {
        iMusicPlayer?.clearPlayQueue()
    }

    fun updatePlayMode(): Int {
        return iMusicPlayer?.updatePlayMode() ?: 0
    }

    fun getLyricRowBeans(): List<LyricRowBean>? {
        return iMusicPlayer?.getLyricRowBeans()
    }

    fun registerCallback(callback: IPlayerStateCallback) {
        iMusicPlayer?.registerCallback(callback)
    }

    fun unregisterCallback(callback: IPlayerStateCallback) {
        iMusicPlayer?.unregisterCallback(callback)
    }

    fun bindService(context: Context, connected: () -> Unit = {}, disconnected: () -> Unit = {}): BindToken? {
        context.startService(Intent(context, MusicPlayerService::class.java))
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                iMusicPlayer = IMusicPlayer.Stub.asInterface(service)
                connected()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                disconnected()
                iMusicPlayer = null
            }
        }
        return if (context.bindService(
                        Intent(context, MusicPlayerService::class.java),
                        serviceConnection,
                        Context.BIND_IMPORTANT
                )
        ) {
            BindToken(context, serviceConnection)
        } else {
            null
        }
    }

    fun unbindService(bindToken: BindToken?) {
        bindToken?.serviceConnection?.let {
            bindToken.context.unbindService(it)
        }
    }

    class BindToken(
            val context: Context,
            val serviceConnection: ServiceConnection
    )

}
