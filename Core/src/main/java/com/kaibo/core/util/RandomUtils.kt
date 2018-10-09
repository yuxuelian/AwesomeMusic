package com.kaibo.core.util

import java.util.*

/**
 * @author kaibo
 * @date 2018/6/27 14:51
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

val random by lazy {
    Random()
}

/**
 * 返回  [start,end) 中的随机数数
 */
fun getRandom(start: Int, end: Int): Int {
    return random.nextInt(end - start) + start
}
