package com.kaibo.core

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.kaibo.core.toast.ToastUtils
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import me.yokeyword.fragmentation.Fragmentation

/**
 * @author:Administrator
 * @date:2018/3/16 0016 下午 1:45
 * GitHub:
 * email:
 * description:
 */
abstract class BaseApplication : Application() {

    companion object {

        private lateinit var baseApplication: BaseApplication

        val INSTANCE
            get() = baseApplication
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()
//        //初始化配置BaseURL
//        HttpRequestManager.BASE_URL = getBaseUrl()
        baseApplication = this
        ToastUtils.init(this)
        Logger.addLogAdapter(AndroidLogAdapter())

        Fragmentation.builder().stackViewMode(Fragmentation.BUBBLE).debug(BuildConfig.DEBUG).install()
    }

//    abstract fun getBaseUrl(): String

}