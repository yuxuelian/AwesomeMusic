package com.kaibo.music.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.kaibo.core.http.HttpRequestManager
import com.kaibo.core.util.saveToFile
import com.kaibo.core.util.toBitmap
import com.kaibo.core.util.toMainThread
import com.kaibo.core.util.toMd5
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File

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
                    try {
                        val localImage: File? = if (FileUtils.isSDcardAvailable()) {
                            File(FileUtils.getImageCacheDir(), imageUrl.toMd5())
                        } else {
                            null
                        }

                        localImage?.let {
                            // 图片已经存在了
                            if (it.exists()) {
                                emitter.onNext(it.toBitmap())
                                emitter.onComplete()
                                return@create
                            }
                        }

                        // 缓存不存在,从网络获取改图片
                        val request = Request.Builder().url(imageUrl).build()
                        val response: Response = HttpRequestManager.okHttpClient.newCall(request).execute()
                        if (response.isSuccessful) {
                            val responseBody: ResponseBody? = response.body()
                            responseBody?.let { it: ResponseBody ->
                                val bitmap: Bitmap = BitmapFactory.decodeStream(it.byteStream())
                                // 缓存到磁盘
                                localImage?.let {
                                    bitmap.saveToFile(it)
                                }
                                // 发射bitmap出去
                                emitter.onNext(bitmap)
                            }
                        }
                        emitter.onComplete()
                    } catch (e: Throwable) {
                        emitter.onError(e)
                    }
                }
                .subscribeOn(Schedulers.io())
                .toMainThread()
    }

}