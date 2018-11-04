package com.kaibo.core.util

import android.util.Base64
import java.nio.charset.Charset

/**
 * @author kaibo
 * @date 2018/11/3 19:48
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */


/**
 * base64编码
 */
@JvmOverloads
fun ByteArray.encodeToString(flags: Int = Base64.DEFAULT): String = Base64.encodeToString(this, flags)

/**
 * base64解码
 */
@JvmOverloads
fun String.decode(flags: Int = Base64.DEFAULT, charset: Charset = Charsets.UTF_8) = String(Base64.decode(this, flags), charset)

