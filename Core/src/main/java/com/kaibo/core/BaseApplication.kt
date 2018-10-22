package com.kaibo.core

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import androidx.room.Room
import com.kaibo.core.http.HttpRequestManager
import com.kaibo.core.toast.ToastUtils
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

/**
 * @author:Administrator
 * @date:2018/3/16 0016 下午 1:45
 * GitHub:
 * email:
 * description:
 */
abstract class BaseApplication : Application() {

    companion object {
        val baseApplication by lazy {
            INSTANCE
        }
        private lateinit var INSTANCE: BaseApplication
    }

    override fun attachBaseContext(base: Context?) {
        MultiDex.install(this)
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()

//        //初始化配置BaseURL
//        HttpRequestManager.BASE_URL = getBaseUrl()
        INSTANCE = this
//        ToastUtils.init(this)
        Logger.addLogAdapter(AndroidLogAdapter())
    }

//    abstract fun getBaseUrl(): String

}