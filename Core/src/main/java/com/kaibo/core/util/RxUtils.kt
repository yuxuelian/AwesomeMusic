package com.kaibo.core.util

import android.view.View
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

fun <T> bindToAutoDispose(lifecycleOwner: LifecycleOwner): AutoDisposeConverter<T> {
    return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, Lifecycle.Event.ON_DESTROY))
}

fun <T> Observable<T>.toMainThread(): Observable<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
}

/**
 * 检查  BaseBean  中的参数
 */
fun <T> Observable<BaseBean<T>>.checkResult(): Observable<T> {
    return this.map {
        if (it.code == 0) {
            it.data
        } else {
            throw DataException(it.code, it.message)
        }
    }
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
fun View.easyClick(autoDispose: AutoDisposeConverter<Unit>, timeout: Long = 200L): ObservableSubscribeProxy<Unit> {
    return this.clicks().debounce(timeout, TimeUnit.MILLISECONDS).toMainThread().`as`(autoDispose)
}
