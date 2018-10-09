package com.kaibo.core.util

import android.net.Uri
import com.kaibo.core.http.HttpRequestManager
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author:Administrator
 * @date:2018/4/2 0002 上午 11:50
 * GitHub:
 * email:
 * description:
 */

/**
 * 判断当前字符串是否是空   如果即不等于  ""  也不等于  "null"  那么认为他不是null
 */
fun String.isNotEmpty() = this != "" && this.toLowerCase() != "null"

/**
 * 将  String  转成  RequestBody
 */
fun String.toJsonRequestBody(): RequestBody = RequestBody.create(HttpRequestManager.JSON, this)

/**
 * 将路径列表转换成  List<MultipartBody.Part>
 *  适用于后台一个key接收文件数组的情况
 */
fun List<String>.toMultiBodyParts(key: String, mediaType: MediaType?) = this
        .filter { it.isNotEmpty() }
        .map {
            val file = File(it)
            MultipartBody.Part.createFormData(key, file.name, RequestBody.create(mediaType, file))
        }

fun String.toFile() = File(this)

fun String.toUri(): Uri {
    return Uri.parse(this)
}

fun String.toMd5(): String {
    val md5: MessageDigest = MessageDigest.getInstance("MD5")
    md5.update(this.toByteArray(charset("UTF-8")))
    val encryption: ByteArray = md5.digest()
    val strBuf = StringBuffer()
    encryption.forEach {
        val enc = it.toInt()
        if (Integer.toHexString(0xFF and enc).length == 1) {
            strBuf.append("0").append(Integer.toHexString(0xFF and enc))
        } else {
            strBuf.append(Integer.toHexString(0xFF and enc))
        }
    }
    return strBuf.toString()
}

/**
 * 将时间字符串对象转换成Long
 */
fun String.toTimeMillis(format: String = "yyyy-MM-dd HH:mm:ss"): Long {
    val dateFormat = SimpleDateFormat(format, Locale.CHINESE)
    return dateFormat.parse(this).time
}

