package com.kaibo.core.util

import android.app.Service
import android.content.Context

/**
 * @author kaibo
 * @date 2018/10/29 11:09
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

inline fun <reified T : Service> Context.startService(vararg params: Pair<String, Any?>) =
        this.startService(this.createIntent(T::class.java, params))
