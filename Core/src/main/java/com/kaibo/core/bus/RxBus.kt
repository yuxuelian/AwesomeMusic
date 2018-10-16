package com.kaibo.core.bus

import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable


/**
 * @author kaibo
 * @date 2018/7/2 17:24
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

object RxBus {
    val mBus: Relay<Any> = PublishRelay.create<Any>().toSerialized()

    fun post(obj: Any) {
        mBus.accept(obj)
    }

    inline fun <reified T> toObservable(): Observable<T> {
        return mBus.ofType(T::class.java)
    }
}