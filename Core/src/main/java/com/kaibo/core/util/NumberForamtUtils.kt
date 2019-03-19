package com.kaibo.core.util

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


private val HEX_CHAR_ARRAY = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

/**
 * 字节转16进制字符串
 */
fun Byte.toHexString(): String {
    val temp = this.toInt() and 0xFF
    return "${HEX_CHAR_ARRAY[temp.ushr(4)]}${HEX_CHAR_ARRAY[temp and 0xF]}"
}

/**
 * 字符转数字  如 '0'  ---->   0
 * 只有0-9字符可以转换   其他字符返回 -1
 */
fun Char.toInt2() = if (this in '0'..'9') {
    this.toShort() - 48
} else {
    throw IllegalArgumentException("只能转换数字字符")
}
