package com.kaibo.core.util

import android.arch.lifecycle.LifecycleOwner
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.AutoDisposeConverter
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.kaibo.core.exception.DataException
import com.kaibo.core.http.BaseBean
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers


/**
 * @author kaibo
 * @date 2018/6/25 10:39
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

fun <T> bindToAutoDispose(lifecycleOwner: LifecycleOwner): AutoDisposeConverter<T> {
    return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner))
}

fun <T> Observable<T>.toMainThread(): Observable<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
}

/**
 * 检查  BaseBean  中的参数
 */
fun <T> Observable<BaseBean<T>>.checkResult(): Observable<T> {
    return this.map {
        if (it.code == 200) {
            it.data
        } else {
            throw DataException(it.code, it.msg)
        }
    }
}