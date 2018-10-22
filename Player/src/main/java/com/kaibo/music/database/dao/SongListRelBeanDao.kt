package com.kaibo.music.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kaibo.music.bean.SongListRelBean
import io.reactivex.Flowable

/**
 * @author kaibo
 * @date 2018/10/22 12:27
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@Dao
interface SongListRelBeanDao {

    @Query("select * from song_list_rel where play_list_id = :playListId")
    fun findByPlayListId(playListId: String): Flowable<List<SongListRelBean>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveOrUpdate(songListRelBean: SongListRelBean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveList(songListRelBean: List<SongListRelBean>)

    @Query("delete from song_list_rel where mid=:mid and play_list_id = :playListId")
    fun deleteByMidAndListId(mid: String, playListId: String)

}