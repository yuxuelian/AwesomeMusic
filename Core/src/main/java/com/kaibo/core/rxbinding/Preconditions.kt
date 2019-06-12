package com.yishi.core.rxbinding

import android.os.Looper
import io.reactivex.Observer
import io.reactivex.disposables.Disposables

/**
 * @author:kaibo
 * @date 2019/5/20 15:40
 * @GitHub：https://github.com/yuxuelian
 * @qq：568966289
 * @description：
 */

internal fun checkMainThread(observer: Observer<*>): Boolean {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        observer.onSubscribe(Disposables.empty())
        observer.onError(IllegalStateException("Expected to be called on the main thread but was " + Thread.currentThread().name))
        return false
    }
    return true
}