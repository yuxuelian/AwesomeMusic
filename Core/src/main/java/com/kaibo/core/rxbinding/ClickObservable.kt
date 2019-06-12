package com.yishi.core.rxbinding

import android.view.View
import androidx.annotation.CheckResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

/**
 * @author:kaibo
 * @date 2019/5/20 15:40
 * @GitHub：https://github.com/yuxuelian
 * @qq：568966289
 * @description：
 */


@CheckResult
fun View.clicks(): Observable<Unit> {
    return ViewClickObservable(this)
}

private class ViewClickObservable(
        private val view: View
) : Observable<Unit>() {

    override fun subscribeActual(observer: Observer<in Unit>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(view, observer)
        observer.onSubscribe(listener)
        view.setOnClickListener(listener)
    }

    private class Listener(
            private val view: View,
            private val observer: Observer<in Unit>
    ) : MainThreadDisposable(), View.OnClickListener {

        override fun onClick(v: View) {
            if (!isDisposed) {
                observer.onNext(Unit)
            }
        }

        override fun onDispose() {
            view.setOnClickListener(null)
        }
    }
}