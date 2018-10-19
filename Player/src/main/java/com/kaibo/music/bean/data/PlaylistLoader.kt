package com.kaibo.music.bean.data

import com.kaibo.music.bean.Playlist
import com.kaibo.music.bean.SongBean
import com.kaibo.music.common.Constants

/**
 * 作者：yonglong on 2016/11/6 17:02
 */
object PlaylistLoader {
    private val TAG = "PlaylistLoader"

    /**
     * 获取全部歌单
     *
     * @param context
     * @return
     */
    fun getAllPlaylist(): List<Playlist> {
//        return DaoLitepal.getAllPlaylist()
        return emptyList()
    }

    fun getPlaylist(pid: String): Playlist {
//        return DaoLitepal.getPlaylist(pid)
        return Playlist()
    }

    fun getHistoryPlaylist(): Playlist {
//        return DaoLitepal.getPlaylist(Constants.PLAYLIST_HISTORY_ID)
        return Playlist()
    }

    fun getFavoritePlaylist(): Playlist {
//        return DaoLitepal.getPlaylist(Constants.PLAYLIST_LOVE_ID)
        return Playlist()
    }

    /**
     * 新增歌单
     *
     * @param name
     * @return
     */
    fun createDefaultPlaylist(pid: String, name: String): Boolean {
        val playlist = Playlist()
        playlist.pid = pid
        playlist.date = System.currentTimeMillis()
        playlist.updateDate = System.currentTimeMillis()
        playlist.name = name
        if (pid != Constants.PLAYLIST_QUEUE_ID)
            playlist.order = "updateDate desc"
//        return DaoLitepal.saveOrUpdatePlaylist(playlist)
        return true
    }

    /**
     * 扫描歌单歌曲
     */
    fun getMusicForPlaylist(pid: String, order: String?): MutableList<SongBean> {
//        return if (order == null) {
//            DaoLitepal.getMusicList(pid)
//        } else {
//            DaoLitepal.getMusicList(pid, order)
//        }
        return ArrayList()
    }

    fun addMusicList(pid: String, musicList: List<SongBean>): Boolean {
        for (music in musicList) {
            addToPlaylist(pid, music)
        }
        return true
    }

    /**
     * 添加歌曲到歌单
     */
    fun addToPlaylist(pid: String, music: SongBean): Boolean {
        try {
//            return DaoLitepal.addToPlaylist(music, pid)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 移除歌曲
     */
    fun removeSong(pid: String, mid: String) {
        try {
//            DaoLitepal.removeSong(pid, mid)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 删除歌单
     */
    fun deletePlaylist(playlist: Playlist) {
        try {
//            DaoLitepal.deletePlaylist(playlist)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }


    /**
     * 清空播放列表
     */
    fun clearPlaylist(pid: String) {
        try {
//            DaoLitepal.clearPlaylist(pid)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
