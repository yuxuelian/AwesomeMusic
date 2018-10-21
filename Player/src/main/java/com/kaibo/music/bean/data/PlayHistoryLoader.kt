package com.kaibo.music.bean.data

import com.kaibo.music.bean.SongBean

/**
 * 作者：yonglong on 2016/11/4 22:30
 */
object PlayHistoryLoader {

    private val TAG = "PlayQueueLoader"

    /**
     * 添加歌曲到播放历史
     */
    fun addSongToHistory(music: SongBean) {
        try {
//            DaoLitepal.addToPlaylist(music, LAYLIST_HISTORY_ID)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 获取播放历史
     */
    fun getPlayHistory(): MutableList<SongBean> {
//        return DaoLitepal.getMusicList(PLAYLIST_HISTORY_ID, "updateDate desc")
        return ArrayList()
    }

    /**
     * 清除播放历史
     */
    fun clearPlayHistory() {
        try {
//            DaoLitepal.clearPlaylist(PLAYLIST_HISTORY_ID)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
