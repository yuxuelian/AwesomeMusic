package com.kaibo.music

import com.kaibo.core.BaseApplication
import com.kaibo.swipe_back.SwipeBackManager
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
        LeakCanary.install(this)

        // 初始化侧滑返回
        SwipeBackManager.init(this)
    }

}