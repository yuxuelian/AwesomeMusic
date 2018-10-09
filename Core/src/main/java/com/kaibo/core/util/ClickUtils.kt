package com.kaibo.core.util

/**
 * @author:Administrator
 * @date:2018/5/14 0014 上午 10:47
 * @GitHub:https://github.com/yuxuelian
 * @email:
 * @description:
 */

private var mExitTime: Long = 0

/**
 * 双击判断
 */
fun isDoubleClick(duration: Long = 2000) = if (System.currentTimeMillis() - mExitTime > duration) {
    mExitTime = System.currentTimeMillis()
    false
} else {
    true
}