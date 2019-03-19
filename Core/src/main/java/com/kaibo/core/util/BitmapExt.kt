package com.kaibo.core.util

import android.content.Context
import android.graphics.*
import android.util.Base64
import kotlinx.io.ByteArrayOutputStream
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

fun Bitmap.toBase64(): String {
    // 要返回的字符串
    ByteArrayOutputStream().use {
        this.compress(Bitmap.CompressFormat.PNG, 100, it)
        // 转换为字符串
        return "data:image/png;base64,${Base64.encodeToString(it.toByteArray(), Base64.DEFAULT).trim()}"
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

fun Bitmap.decorate(size: Float): Bitmap {
    val resBitmap = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_4444)
    val canvas = Canvas(resBitmap)
    // 画中间的二维码
    val qrCodeSize = (355 / 515.0 * size).toInt()
    val bitmapLeft = (80 / 515.0 * size).toFloat()
    val bitmapTop = (80 / 515.0 * size).toFloat()
    canvas.drawBitmap(this.resize(qrCodeSize, qrCodeSize), bitmapLeft, bitmapTop, null)
    // 线宽
    val lineWidth = (10 / 515.0 * size).toFloat()
    val paint = Paint()
    paint.strokeWidth = lineWidth
    paint.style = Paint.Style.FILL
    val leftRightWidth = (70 / 515.0 * size).toFloat()
    // 画左上角第一个矩形
    paint.color = Color.parseColor("#D00000")
    canvas.drawRect(RectF(0f, 0f, leftRightWidth, leftRightWidth), paint)
    // 画右侧矩形
    paint.color = Color.parseColor("#ECCC1F")
    canvas.drawRect(RectF(size - leftRightWidth, size - leftRightWidth, size, size), paint)
    // 画左侧底部矩形
    paint.color = Color.parseColor("#008FEB")
    canvas.drawRect(RectF(0f, size - leftRightWidth, leftRightWidth, size), paint)
    return resBitmap
}

///**
// * 自定义模糊图片   只能模糊本地图片   从网络获取的bitmap需要先保存到本地   否则不能模糊成功
// * 耗时操作    请在子线程执行
// *
// * @param scale  缩放大小 (>1.0), 例如若想将输入图片放大2倍然后再模糊(提高模糊效率), 则传入2.
// * @param radius       缩放半径 (0.0 , 25.0]
// * @param context
// * @param mInputBitmap
// * @param mTargetView
// * @return
// */
//fun Bitmap.blur(context: Context,
//                @androidx.annotation.FloatRange(from = 1.0, fromInclusive = false) scale: Float = 4.0f,
//                @androidx.annotation.FloatRange(from = 0.0, to = 25.0, fromInclusive = false) radius: Float = 25f): Bitmap {
//    if (scale < 1F) {
//        throw IllegalArgumentException("Value must be > 0.0 (was $scale)")
//    }
//
//    if (radius <= 0.0f || radius > 25.0f) {
//        throw IllegalArgumentException("Value must be > 0.0 and ≤ 25.0 (was $radius)")
//    }
//
//    val startTime = System.currentTimeMillis()
//    //首先将图片缩小一下  1.提高模糊效率   2.防止内存溢出
//    val outputBitmap = this.resizeOfScale(1.0F / scale)
//    val rs = RenderScript.create(context)
//    val input = Allocation.createFromBitmap(rs, outputBitmap)
//    val output = Allocation.createTyped(rs, input.type)
//
//    //开始模糊操作
//    ScriptIntrinsicBlur.create(rs, Element.U8_4(rs)).run {
//        setRadius(radius)
//        setInput(input)
//        forEach(output)
//    }
//
//    output.copyTo(outputBitmap)
//    rs.destroy()
//    Log.d(TAG, "模糊一共耗时:${(System.currentTimeMillis() - startTime)}ms")
//    return outputBitmap
//}
