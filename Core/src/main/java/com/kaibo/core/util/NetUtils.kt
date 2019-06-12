package com.kaibo.core.util

import retrofit2.HttpException

/**
 * @author kaibo
 * @date 2018/8/1 15:17
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

fun HttpException.errorBodyMsg(): String? {
    return this.response().errorBody()?.string() ?: ""
}
