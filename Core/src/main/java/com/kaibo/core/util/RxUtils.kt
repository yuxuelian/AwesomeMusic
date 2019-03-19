package com.kaibo.core.util

import android.view.View
import androidx.annotation.CheckResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.exception.DataException
import com.kaibo.core.http.BaseBean
import com.orhanobut.logger.Logger
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.AutoDisposeConverter
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * @author kaibo
 * @date 2018/6/25 10:39
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@CheckResult
fun <T> LifecycleOwner.bindLifecycle(): AutoDisposeConverter<T> {
    // 在OnDestroy的时候解除绑定
    return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY))
}

@CheckResult
fun <T> Observable<T>.toMainThread(): Observable<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
}

@CheckResult
fun <T> Observable<T>.async(): Observable<T> {
    return this.subscribeOn(Schedulers.io()).toMainThread()
}

@CheckResult
fun <T> Flowable<T>.toMainThread(): Flowable<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
}

@CheckResult
fun <T> Flowable<T>.async(): Flowable<T> {
    return this.subscribeOn(Schedulers.io()).toMainThread()
}

/**
 * 检查  BaseBean  中的参数
 */
@CheckResult
fun <T> Observable<BaseBean<T>>.checkResult(): Observable<T> {
    return this.map {
        Logger.d(it)
        if (it.code == 0) {
            it.data
        } else {
            throw DataException(it.code, it.message)
        }
    }
}

/**
 * 延时指定的时长  单位毫秒
 */
@CheckResult
fun Long.delay(autoDispose: AutoDisposeConverter<Long>): ObservableSubscribeProxy<Long> {
    return Observable.timer(this, TimeUnit.MILLISECONDS).toMainThread().`as`(autoDispose)
}

fun <T> singleAsync(autoDispose: AutoDisposeConverter<T>, onSuccess: (T) -> Unit = {}, task: () -> T) {
    Observable
            .create<T> {
                try {
                    it.onNext(task.invoke())
                    it.onComplete()
                } catch (e: Throwable) {
                    it.onError(e)
                }
            }
            .subscribeOn(Schedulers.single())
            .toMainThread()
            .`as`(autoDispose)
            .subscribe({
                onSuccess.invoke(it)
                Logger.d("异步任务执行成功")
            }) {
                it.printStackTrace()
            }
}

/**
 * 带防重复点击的方法
 */
@CheckResult
fun View.easyClick(autoDispose: AutoDisposeConverter<Unit>, timeout: Long = 200L): ObservableSubscribeProxy<Unit> {
    return this.clicks().debounce(timeout, TimeUnit.MILLISECONDS).toMainThread().`as`(autoDispose)
}
