package com.kaibo.core.util

import kotlinx.coroutines.*

/**
 * @author kaibo
 * @date 2019/2/18 10:53
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

fun launchUI(start: CoroutineStart = CoroutineStart.DEFAULT,
             block: suspend CoroutineScope.() -> Unit) =
        GlobalScope.launch(Dispatchers.Main, start, block)
