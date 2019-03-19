package com.kaibo.core.util

import java.text.SimpleDateFormat
import java.util.*


/**
 * @author kaibo
 * @date 2018/9/10 17:34
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

/**
 * 一年的毫秒数
 */

val YEAR_OF_MILLIS
    get() = 24 * 60 * 60 * 1000 * 365L

/**
 * 一天的毫秒数
 */
val DAY_OF_MILLIS = 24 * 60 * 60 * 1000L

/**
 * 三天的毫秒数
 */
val THREE_DAY_OF_MILLIS = 3 * DAY_OF_MILLIS

val DATE_FORMAT_STRING = "yyyy年MM月dd日"
val TIME_FORMAT_STRING = "HH时mm分ss秒"

/**
 * 将Long转成时间
 * 住: SimpleDateFormat 不应该被定义为  static  因为SimpleDateFormat是线程非安全的
 */
fun Long.toDate(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val dateFormat = SimpleDateFormat(format, Locale.CHINESE)
    return dateFormat.format(this)
}

/**
 * 将时间字符串对象转换成Long
 */
fun String.toTimeMillis(format: String = "yyyy-MM-dd HH:mm:ss"): Long {
    val dateFormat = SimpleDateFormat(format, Locale.CHINESE)
    return try {
        dateFormat.parse(this).time
    } catch (e: Throwable) {
        0L
    }
}

fun getLastDayOfMonth(year: Int, month: Int): String {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month - 1)
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE))
    return cal.time.time.toDate("yyyy-MM-dd")
}

fun getFirstDayOfMonth(year: Int, month: Int): String {
    return String.format("%04d-%02d-01", year, month)
}
