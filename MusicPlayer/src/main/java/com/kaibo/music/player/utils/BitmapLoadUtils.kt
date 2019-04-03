package com.kaibo.music.player.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * @author kaibo
 * @date 2019/4/3 15:57
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

private val okHttpClient: OkHttpClient by lazy {
    OkHttpClient
            .Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
}

private val HEX_CHAR_ARRAY = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

/**
 * 字节转16进制字符串
 */
private fun Byte.toHexString(): String {
    val temp = this.toInt() and 0xFF
    return "${HEX_CHAR_ARRAY[temp.ushr(4)]}${HEX_CHAR_ARRAY[temp and 0xF]}"
}

private fun String.toMd5(): String {
    return MessageDigest
            .getInstance("MD5")
            .apply { update(toByteArray()) }
            .digest()
            .joinToString("", transform = Byte::toHexString)
}

fun Context.loadBitmap(url: String): Observable<Bitmap> {
    return Observable
            .create<Bitmap> { emit ->
                try {
                    val bitmapFile = File(this.externalCacheDir, url.toMd5())
                    if (!bitmapFile.exists()) {
                        val request: Request = Request.Builder().url(url).build()
                        val response: Response = okHttpClient.newCall(request).execute()
                        if (response.isSuccessful) {
                            val inputStream = response.body()?.byteStream()
                            // 存储到磁盘
                            FileOutputStream(bitmapFile).use {
                                inputStream?.copyTo(it)
                            }
                            inputStream?.close()
                        }
                    }
                    emit.onNext(BitmapFactory.decodeFile(bitmapFile.absolutePath))
                    emit.onComplete()
                } catch (e: Throwable) {
                    emit.onError(e)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}
