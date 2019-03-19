package com.kaibo.core.util

import android.net.Uri
import com.kaibo.core.http.HttpRequestManager
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileWriter
import java.security.MessageDigest

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
fun String.toJsonRequestBody(): RequestBody = RequestBody.create(HttpRequestManager.mediaTypeJson, this)

/**
 * 将路径列表转换成  List<MultipartBody.Part>
 *  适用于后台一个key接收文件数组的情况
 */
fun List<String>.toMultiBodyParts(key: String, mediaType: MediaType): List<MultipartBody.Part> = this
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
    return MessageDigest
            .getInstance("MD5")
            .apply { update(toByteArray()) }
            .digest()
            .joinToString("", transform = Byte::toHexString)
}

fun String.saveToFile(targetFile: File) {
    var fileWriter: FileWriter? = null
    try {
        // 判断是否存在
        if (targetFile.exists()) {
            // 存在就删除  然后重新创建
            targetFile.delete()
        }
        // 重新创建
        targetFile.createNewFile()
        fileWriter = FileWriter(targetFile)
        fileWriter.write(this)
        fileWriter.flush()
    } catch (e: Throwable) {
        e.printStackTrace()
    } finally {
        fileWriter?.close()
    }
}




