package com.kaibo.core.util

import java.io.File
import java.io.FileReader

/**
 * @author kaibo
 * @date 2018/11/3 20:31
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

fun File.readString(): String {
    var fileReader: FileReader? = null
    return try {
        fileReader = FileReader(this)
        val bufferChars = CharArray(8192)
        val stringBuffer = StringBuffer()
        var readLength = 0
        while ({
                    readLength = fileReader.read(bufferChars)
                    readLength
                }.invoke() != -1) {
            stringBuffer.append(bufferChars, 0, readLength)
        }
        stringBuffer.toString()
    } catch (e: Throwable) {
        e.printStackTrace()
        ""
    } finally {
        try {
            fileReader?.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
