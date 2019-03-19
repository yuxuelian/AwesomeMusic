package com.yishi.refresh

import android.content.Context

/**
 * @author kaibo
 * @date 2018/12/4 16:53
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

// 能获取到Content的情况
internal fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()