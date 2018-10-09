package com.kaibo.core.util

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * @author kaibo
 * @date 2018/8/7 19:34
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

fun Bitmap.toBase64(): String {
    // 要返回的字符串
    ByteArrayOutputStream().use {
        this.compress(Bitmap.CompressFormat.PNG, 100, it)
        // 转换为字符串
        return Base64.encodeToString(it.toByteArray(), Base64.DEFAULT)
    }
}
