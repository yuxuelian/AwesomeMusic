package com.kaibo.core.util

/**
 * @author kaibo
 * @date 2019/1/21 16:25
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

sealed class BooleanExt<out T>

class WithData<T>(val data: T) : BooleanExt<T>()
object Otherwise : BooleanExt<Nothing>()

inline fun <T> Boolean.yes(block: () -> T) = when {
    this -> {
        WithData(block())
    }
    else -> {
        Otherwise
    }
}

inline fun <T> Boolean.no(block: () -> T) = when {
    this -> {
        Otherwise
    }
    else -> {
        WithData(block())
    }
}

fun <T> BooleanExt<T>.otherwise(block: () -> T): T = when (this) {
    is WithData -> {
        this.data
    }
    is Otherwise -> {
        block()
    }
}