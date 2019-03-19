package com.kaibo.music.database

import com.kaibo.music.bean.PlayListBean
import com.kaibo.music.bean.SongBean
import com.kaibo.music.bean.SongListRelBean
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

object PlayListHelper {

    private val playListBeanDao = PlayListDatabase.playListBeanDao()
    private val songListRelBeanDao = PlayListDatabase.songListRelBeanDao()
    private val songBeanDao = PlayListDatabase.songBeanDao()

    /**
     * 初始化几个播放歌单
     */
    fun initPlayList() {
        // 初始化创建三个歌单
        playListBeanDao.saveOrUpdate(PlayListBean.love())
        playListBeanDao.saveOrUpdate(PlayListBean.queue())
        playListBeanDao.saveOrUpdate(PlayListBean.history())
    }

    /**
     * 获取所有歌单
     */
    fun getAllPlaylist(): Flowable<List<PlayListBean>> {
        return playListBeanDao.findAll().subscribeOn(Schedulers.io())
    }

    /**
     * 根据Id获取一个歌单
     */
    fun getPlaylist(playListId: String): Flowable<PlayListBean> {
        return playListBeanDao.findById(playListId).subscribeOn(Schedulers.io())
    }

    /**
     * 新增歌单
     *
     * @param name
     * @return
     */
    fun createDefaultPlaylist(playListId: String,
                              listName: String,
                              desc: String = "",
                              imageUrl: String = "") {
        val playListBean = PlayListBean(playListId = playListId,
                listName = listName,
                desc = desc,
                imageUrl = imageUrl,
                createDate = System.currentTimeMillis(),
                updateDate = System.currentTimeMillis())
        playListBeanDao.saveOrUpdate(playListBean)
    }

    /**
     * 根据歌单id获取所有的歌曲信息
     */
    fun getSongListByPlayListId(playListId: String): Flowable<List<SongBean>> {
        return songListRelBeanDao
                .findByPlayListId(playListId)
                .flatMap { it: List<SongListRelBean> ->
                    songBeanDao.findById(it.map { songListRelBean: SongListRelBean ->
                        songListRelBean.mid
                    })
                }
                .subscribeOn(Schedulers.io())
    }

    /**
     * 向指定的歌单中添加一些歌曲
     */
    fun addMusicList(playListId: String, songList: List<SongBean>) {
        songListRelBeanDao.saveList(songList.map { SongListRelBean(0, it.mid, playListId) })
    }

    /**
     * 向指定的歌单中添加一首歌曲
     */
    fun addToPlaylist(songMid: String, layListId: String) {
        songListRelBeanDao.saveOrUpdate(SongListRelBean(0, songMid, layListId))
    }

    /**
     * 从指定的歌单中移出一首歌曲
     */
    fun removeSong(mid: String, playListId: String) {
        songListRelBeanDao.deleteByMidAndListId(mid, playListId)
    }

    /**
     * 删除歌单
     */
    fun deletePlaylist(playListId: String) {
        playListBeanDao.deleteByPlayListId(playListId)
    }

    /**
     * 清空播放列表
     */
    fun clearPlaylist() {
        playListBeanDao.clearAll()
    }
}
