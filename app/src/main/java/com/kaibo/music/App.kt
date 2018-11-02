package com.kaibo.music

import com.kaibo.core.BaseApplication
import com.kaibo.music.database.PlayListDatabase
import com.kaibo.music.database.PlayListHelper
import com.squareup.leakcanary.LeakCanary

/**
 * @author:Administrator
 * @createDate:2018/3/16 0016 下午 3:28
 * GitHub:
 * email:
 * description:
 */

class App : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        // 初始化数据库
        PlayListDatabase.init(this)
        // 初始化播放歌单
        PlayListHelper.initPlayList()


        LeakCanary.install(this)
    }

}