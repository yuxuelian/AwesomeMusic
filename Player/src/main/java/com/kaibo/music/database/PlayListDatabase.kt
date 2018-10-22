package com.kaibo.music.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kaibo.music.bean.PlayListBean
import com.kaibo.music.bean.SongBean
import com.kaibo.music.bean.SongListRelBean
import com.kaibo.music.database.dao.PlayListBeanDao
import com.kaibo.music.database.dao.SongBeanDao
import com.kaibo.music.database.dao.SongListRelBeanDao

/**
 * @author kaibo
 * @date 2018/10/22 11:38
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@Database(entities = [SongBean::class, PlayListBean::class, SongListRelBean::class], version = 1, exportSchema = false)
abstract class PlayListDatabase : RoomDatabase() {

    abstract fun songBeanDao(): SongBeanDao

    abstract fun playListBeanDao(): PlayListBeanDao

    abstract fun songListRelBeanDao(): SongListRelBeanDao

    companion object {
        val INSTANCE
            get() = playListDatabase

        private lateinit var playListDatabase: PlayListDatabase

        fun init(context: Context) {
            // 创建数据库
            playListDatabase = Room
                    .databaseBuilder(context, PlayListDatabase::class.java, "play_database.db")
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                        }
                    })
                    // 允许在主线程操作数据库
                    .allowMainThreadQueries()
//                .addMigrations()
//                .fallbackToDestructiveMigration()
                    .build()
        }
    }

}