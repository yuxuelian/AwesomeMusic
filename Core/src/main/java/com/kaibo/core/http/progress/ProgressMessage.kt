package com.kaibo.core.http.progress

/**
 * @author:Administrator
 * @date:2018/5/31 0031下午 5:48
 * @GitHub:https://github.com/yuxuelian
 * @email:
 * @description:
 */

data class ProgressMessage(
        val currentLength: Long,
        val fillLength: Long,
        val done: Boolean = false
) {
    val rate: Double = (currentLength / fillLength).toDouble()
}