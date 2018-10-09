package com.kaibo.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel


/**
 * @author kaibo
 * @date 2018/6/26 17:51
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：二维码相关操作
 */

object QrCodeUtils {

    /**
     * 生成二维码Bitmap
     *
     * @param content   内容
     * @param size  图片大小
     * @param logoBm    二维码中心的Logo图标（可以为null）
     * @return 生成二维码及保存文件是否成功
     * 这个方法是耗时操作    建议在子线程执行
     */
    fun createQRImage(content: String, size: Int, logoBm: Bitmap? = null): Bitmap {
        if (!content.isNotEmpty() || size < 100) {
            throw IllegalArgumentException("content not is empty or size < 100")
        }
        //配置参数
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.CHARACTER_SET] = "utf-8"
        //容错级别
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
        //设置空白边距的宽度  default is 4
        hints[EncodeHintType.MARGIN] = 0
        // 图像数据转换，使用了矩阵转换
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val pixels = IntArray(size * size)
        // 下面这里按照二维码的算法，逐个生成二维码的图片，
        // 两个for循环是图片横列扫描的结果
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * size + x] = -0x1000000
                } else {
                    pixels[y * size + x] = -0x0000001
                }
            }
        }
        // 生成二维码图片的格式，使用ARGB_8888
        var bitmap: Bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        if (logoBm != null) {
            bitmap = addLogo(bitmap, logoBm)
        }
        return bitmap
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private fun addLogo(src: Bitmap, logo: Bitmap): Bitmap {
        //获取图片的宽高
        val srcWidth = src.width
        val srcHeight = src.height
        val logoWidth = logo.width
        val logoHeight = logo.height
        //logo大小为二维码整体大小的1/5
        val scaleFactor = srcWidth / (5f * logoWidth)
        val bitmap: Bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(src, 0f, 0f, null)
        canvas.scale(scaleFactor, scaleFactor, srcWidth / 2f, srcHeight / 2f)
        canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2f, (srcHeight - logoHeight) / 2f, null)
        return bitmap
    }
}

