package com.kaibo.music.player.player.service

import android.graphics.Bitmap
import com.kaibo.music.player.IMusicPlayer
import com.kaibo.music.player.IPlayerStateCallback
import com.kaibo.music.player.bean.LyricRowBean
import com.kaibo.music.player.bean.SongBean

/**
 * @author kaibo
 */
class IMusicPlayerStub(private val musicPlayerService: MusicPlayerService) : IMusicPlayer.Stub() {

    override fun togglePlayer() {
        musicPlayerService.togglePlayer()
    }

    override fun pause() {
        musicPlayerService.pause()
    }

    override fun play() {
        musicPlayerService.play()
    }

    override fun stop() {
        musicPlayerService.stop()
    }

    override fun prev() {
        musicPlayerService.prev()
    }

    override fun next() {
        musicPlayerService.next()
    }

    override fun getDuration(): Int {
        return musicPlayerService.getDuration()
    }

    override fun seekTo(pos: Int) {
        musicPlayerService.seekTo(pos)
    }

    override fun getCurrentPosition(): Int {
        return musicPlayerService.getCurrentPosition()
    }

    override fun setPlayPosition(pos: Int) {
        musicPlayerService.setPlayPosition(pos)
    }

    override fun getPlayPosition(): Int {
        return musicPlayerService.getPlayPosition()
    }

    override fun setPlaySong(songBean: SongBean) {
        musicPlayerService.setPlaySong(songBean)
    }

    override fun getPlaySong(): SongBean? {
        return musicPlayerService.getPlaySong()
    }

    override fun getSongImage(): Bitmap? {
        return musicPlayerService.getSongImage()
    }

    override fun isPlaying(): Boolean {
        return musicPlayerService.isPlaying()
    }

    override fun isPrepared(): Boolean {
        return musicPlayerService.isPrepared()
    }

    override fun getPlayQueue(): List<SongBean> {
        return musicPlayerService.getPlayQueue()
    }

    override fun setPlayQueue(playQueue: List<SongBean>) {
        musicPlayerService.setPlayQueue(playQueue)
    }

    override fun removeAt(position: Int) {
        musicPlayerService.removeAt(position)
    }

    override fun remove(songBean: SongBean) {
        musicPlayerService.remove(songBean)
    }

    override fun showDesktopLyric() {
        musicPlayerService.showDesktopLyric()
    }

    override fun clearPlayQueue() {
        musicPlayerService.clearPlayQueue()
    }

    override fun updatePlayMode(): Int {
        return musicPlayerService.updatePlayMode()
    }

    override fun getLyricRowBeans(): List<LyricRowBean>? {
        return musicPlayerService.getLyricRowBeans()
    }

    override fun exit() {
        musicPlayerService.exit()
    }

    override fun registerCallback(callback: IPlayerStateCallback) {
        musicPlayerService.registerCallback(callback)
    }

    override fun unregisterCallback(callback: IPlayerStateCallback) {
        musicPlayerService.unregisterCallback(callback)
    }
}