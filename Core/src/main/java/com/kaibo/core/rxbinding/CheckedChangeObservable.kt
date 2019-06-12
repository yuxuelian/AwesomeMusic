package com.yishi.core.rxbinding

import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.annotation.CheckResult
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

/**
 * @author:kaibo
 * @date 2019/5/20 15:41
 * @GitHub：https://github.com/yuxuelian
 * @qq：568966289
 * @description：
 */

@CheckResult
fun CompoundButton.checkedChanges(): InitialValueObservable<Boolean> {
    return CompoundButtonCheckedChangeObservable(this)
}

private class CompoundButtonCheckedChangeObservable(
        private val view: CompoundButton
) : InitialValueObservable<Boolean>() {

    override fun subscribeListener(observer: Observer<in Boolean>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(view, observer)
        observer.onSubscribe(listener)
        view.setOnCheckedChangeListener(listener)
    }

    override val initialValue get() = view.isChecked

    private class Listener(
            private val view: CompoundButton,
            private val observer: Observer<in Boolean>
    ) : MainThreadDisposable(), OnCheckedChangeListener {

        override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
            if (!isDisposed) {
                observer.onNext(isChecked)
            }
        }

        override fun onDispose() {
            view.setOnCheckedChangeListener(null)
        }
    }
}