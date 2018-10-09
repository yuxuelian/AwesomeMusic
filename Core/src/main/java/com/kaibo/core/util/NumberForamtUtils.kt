package com.kaibo.core.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author:Administrator
 * @date:2018/4/2 0002 下午 3:49
 * GitHub:
 * email:
 * description:
 */

/**
 * 对Double保留两位小数
 */
fun Double.leaveTwoDecimal(): Double = Math.round(this * 100) / 100.0

/**
 * 字符转数字  如 '0'  ---->   0
 * 只有0-9字符可以转换   其他字符返回 -1
 */
fun Char.toInt2() = if (this in '0'..'9') {
    this.toInt() - 48
} else {
    throw IllegalArgumentException("只能转换数字字符")
}

/**
 * 将Long转成时间
 */
fun Long.toDate(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val dateFormat = SimpleDateFormat(format, Locale.CHINA)
    return dateFormat.format(this)
}


