package com.kaibo.music.database.dao

import androidx.room.*
import com.kaibo.music.bean.SongBean
import io.reactivex.Flowable

/**
 * @author kaibo
 * @date 2018/10/22 11:51
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@Dao
interface SongBeanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveOrUpdate(songBean: SongBean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveList(songList: List<SongBean>)

    @Delete
    fun delete(songBean: SongBean)

    @Query("select * from song where mid=:mid")
    fun findById(mid: String): Flowable<SongBean>

    @Query("select * from song where mid in (:midList)")
    fun findById(midList: List<String>): Flowable<List<SongBean>>

    @Query("delete from song")
    fun clearAll()
}
