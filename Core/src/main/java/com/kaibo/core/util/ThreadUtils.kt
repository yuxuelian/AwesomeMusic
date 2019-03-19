package com.kaibo.core.util

import java.util.concurrent.*

/**
 * @author:Administrator
 * @date:2018/5/14 0014 下午 3:24
 * @GitHub:https://github.com/yuxuelian
 * @email:
 * @description:
 */

private val MAX_POOL_SIZ = Runtime.getRuntime().availableProcessors()
private const val CORE_POOL_SIZE = 0
private const val KEEP_ALIVE_TIME = 60L

private fun threadFactory(name: String, daemon: Boolean = false) = ThreadFactory {
    val result = Thread(it, name)
    result.isDaemon = daemon
    result
}

/**
 * 普通线程池
 */
val executorService: ExecutorService by lazy {
    ThreadPoolExecutor(CORE_POOL_SIZE,
            MAX_POOL_SIZ,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            SynchronousQueue(),
            threadFactory("THREAD-POOL"))
}

/**
 * 调度线程池
 */
val scheduledExecutorService: ScheduledExecutorService by lazy {
    Executors.newScheduledThreadPool(CORE_POOL_SIZE, threadFactory("THREAD-SCHEDULED"))
}