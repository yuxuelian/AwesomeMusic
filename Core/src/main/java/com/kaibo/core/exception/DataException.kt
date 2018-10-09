package com.kaibo.core.exception

/**
 * @author kaibo
 * @date 2018/6/29 10:50
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class DataException(val code: Int, val msg: String) : RuntimeException(msg)
