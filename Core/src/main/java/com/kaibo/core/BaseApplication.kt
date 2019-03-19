package com.kaibo.core

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import androidx.multidex.MultiDex
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

private lateinit var AppInstance: BaseApplication

object AppContext : ContextWrapper(AppInstance)

abstract class BaseApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()
        AppInstance = this
        ToastUtils.init(this)
        Logger.addLogAdapter(AndroidLogAdapter())
    }
}