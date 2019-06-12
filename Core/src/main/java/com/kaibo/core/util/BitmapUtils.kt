package com.kaibo.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * @author kaibo
 * @date 2018/8/7 19:34
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

private const val TAG = "BitmapUtilKt"

fun Bitmap.toBase64(): String {
    // 要返回的字符串
    ByteArrayOutputStream().use {
        this.compress(Bitmap.CompressFormat.PNG, 100, it)
        // 转换为字符串
        return Base64.encodeToString(it.toByteArray(), Base64.DEFAULT)
    }
}


/**
 * 将 Bitmap 保存到指定的文件
 * @param file 保存到指定的文件
 */
fun Bitmap.saveToFile(file: File) {
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    val out = FileOutputStream(file)
    this.compress(Bitmap.CompressFormat.PNG, 90, out)
    out.flush()
    out.close()
}


/**
 * 从磁盘上获取Bitmap
 *
 * @param path
 * @param name
 * @return
 */
fun File.toBitmap(): Bitmap = BitmapFactory.decodeStream(FileInputStream(this))


/**
 * 按比例缩放bitmap
 *
 * @param bitmap
 * @param scale  scale>1是放大   scale<1  是缩小
 * @return
 */
fun Bitmap.resizeOfScale(scale: Float): Bitmap {
    if (scale <= 0) {
        throw IllegalArgumentException("scale must be > 0")
    }

    val width = this.width
    val height = this.height

    val newWidth = width * scale
    val newHeight = height * scale

    val scaleWidth = newWidth / width
    val scaleHeight = newHeight / height

    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

/**
 * 指定尺寸缩放bitmap
 *
 * @param bitmap
 * @param w
 * @param h
 * @return
 */
fun Bitmap.resize(w: Int, h: Int): Bitmap {
    val width = this.width
    val height = this.height

    val scaleWidth = w.toFloat() / width
    val scaleHeight = h.toFloat() / height

    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

/**
 * 保持图片不变形  从左上角裁剪图片到指定的宽高比
 * @param aspectRatio 宽高比
 */
fun Bitmap.clipTo(aspectRatio: Double): Bitmap {
    val currentAspectRatio = this.width.toDouble() / this.height
    return when {
        currentAspectRatio > aspectRatio -> {
            Bitmap.createBitmap(this,
                    0,
                    0,
                    (this.height * aspectRatio + .5).toInt(),
                    this.height)
        }
        currentAspectRatio == aspectRatio -> {
            this
        }
        else -> {
            Bitmap.createBitmap(this,
                    0,
                    0,
                    this.width,
                    (this.width / aspectRatio + .5).toInt())
        }
    }
}


/**
 * 自定义模糊图片   只能模糊本地图片   从网络获取的bitmap需要先保存到本地   否则不能模糊成功
 * 耗时操作    请在子线程执行
 *
 * @param scale  缩放大小 (>1.0), 例如若想将输入图片放大2倍然后再模糊(提高模糊效率), 则传入2.
 * @param radius       缩放半径 (0.0 , 25.0]
 * @param context
 * @param mInputBitmap
 * @param mTargetView
 * @return
 */
fun Bitmap.blur(context: Context, scale: Float = 4.0f, radius: Float = 25f): Bitmap {
    if (scale < 1F) {
        throw IllegalArgumentException("Value must be > 0.0 (was $scale)")
    }
    if (radius <= 0.0f || radius > 25.0f) {
        throw IllegalArgumentException("Value must be > 0.0 and ≤ 25.0 (was $radius)")
    }
    val startTime = System.currentTimeMillis()
    //首先将图片缩小一下  1.提高模糊效率   2.防止内存溢出
    val outputBitmap = this.resizeOfScale(1.0F / scale)
    val rs = RenderScript.create(context)
    val input = Allocation.createFromBitmap(rs, outputBitmap)
    val output = Allocation.createTyped(rs, input.type)
    //开始模糊操作
    ScriptIntrinsicBlur.create(rs, Element.U8_4(rs)).run {
        setRadius(radius)
        setInput(input)
        forEach(output)
    }
    output.copyTo(outputBitmap)
    rs.destroy()
    Log.d(TAG, "模糊一共耗时:${(System.currentTimeMillis() - startTime)}ms")
    return outputBitmap
}
