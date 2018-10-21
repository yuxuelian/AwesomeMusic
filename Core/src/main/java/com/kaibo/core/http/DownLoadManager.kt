package com.kaibo.core.http

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.kaibo.core.util.toMainThread
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody

/**
 * @author Administrator
 * @date 2018/10/21 22:27
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

object DownLoadManager {

    fun downImage(imageUrl: String): Observable<Bitmap> {
        return Observable
                .create { emitter: ObservableEmitter<Bitmap> ->
                    val request = Request.Builder().url(imageUrl).build()
                    try {
                        val response: Response = HttpRequestManager.okHttpClient.newCall(request).execute()
                        if (response.isSuccessful) {
                            val responseBody: ResponseBody? = response.body()
                            responseBody?.let {
                                emitter.onNext(BitmapFactory.decodeStream(it.byteStream()))
                                emitter.onComplete()
                            }
                        }
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
                .subscribeOn(Schedulers.io())
                .toMainThread()
    }

}