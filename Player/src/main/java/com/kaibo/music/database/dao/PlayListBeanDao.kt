package com.kaibo.music.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kaibo.music.bean.PlayListBean
import io.reactivex.Flowable

/**
 * @author kaibo
 * @date 2018/10/22 11:51
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@Dao
interface PlayListBeanDao {

    @Query("select * from play_list")
    fun findAll(): Flowable<List<PlayListBean>>

    @Query("select * from play_list where play_list_id=:playListId")
    fun findById(playListId: String): Flowable<PlayListBean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveOrUpdate(playListBean: PlayListBean)

    @Query("delete from play_list where play_list_id=:playListId")
    fun deleteByPlayListId(playListId: String)

    @Query("delete from play_list")
    fun clearAll()
}