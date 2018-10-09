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
    private val mBus: Relay<Any> = PublishRelay.create<Any>().toSerialized()

    fun post(obj: Any) {
        mBus.accept(obj)
    }

    fun <T> toObservable(tClass: Class<T>): Observable<T> {
        return mBus.ofType(tClass)
    }

    fun toObservable(): Observable<Any> {
        return mBus
    }

    fun hasObservers(): Boolean {
        return mBus.hasObservers()
    }
}