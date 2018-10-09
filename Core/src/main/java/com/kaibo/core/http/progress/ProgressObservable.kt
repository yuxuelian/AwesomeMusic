package com.kaibo.core.http.progress

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentHashMap

/**
 * @author:Administrator
 * @date:2018/6/1 0001上午 9:06
 * @GitHub:https://github.com/yuxuelian
 * @email:
 * @description:
 */

object ProgressObservable {

    private val observableEmitters: MutableMap<String, ObservableEmitter<ProgressMessage>> by lazy {
        ConcurrentHashMap<String, ObservableEmitter<ProgressMessage>>()
    }

    fun listener(key: String): Observable<ProgressMessage> {
        return Observable
                .create<ProgressMessage> {
                    observableEmitters[key] = it
                }
                .doOnComplete {
                    observableEmitters.remove(key)
                }
                .doOnError {
                    observableEmitters.remove(key)
                }
//                .toMainThread()
    }

    operator fun get(key: String): ObservableEmitter<ProgressMessage>? = observableEmitters[key]
}

