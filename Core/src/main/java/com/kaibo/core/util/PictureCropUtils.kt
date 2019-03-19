package com.kaibo.core.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kaibo.core.R
import com.yalantis.ucrop.UCrop
import java.io.File

/**
 * @author kaibo
 * @date 2018/11/15 11:16
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

private const val TAG = "PictureSelectUtilsKt"

private var mCropCallBack: ((res: Uri) -> Unit)? = null

/**
 * Fragment中启动裁剪
 */
fun Fragment.startCrop(sourceUri: Uri, callBack: ((res: Uri) -> Unit)? = null) {
    mCropCallBack = callBack
    context?.let {
        val options = UCrop.Options()
        val themeColor = ContextCompat.getColor(it, R.color.theme_blue)
        options.setToolbarColor(themeColor)
        options.setStatusBarColor(themeColor)
        // 显示圆形覆盖层
        options.setCircleDimmedLayer(true)
        options.setHideBottomControls(false)
        UCrop
                .of(sourceUri, Uri.fromFile(File(requireActivity().filesDir, "destination-crop-bitmap.jpg")))
                .withAspectRatio(1f, 1f)
                .withOptions(options)
                .withMaxResultSize(500, 500)
                .start(it, this)
    }
}

/**
 * 处理裁剪的返回值
 */
fun Fragment.handleCropResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode == Activity.RESULT_OK) {
        when (requestCode) {
            UCrop.REQUEST_CROP -> {
                //裁剪成功
                data?.let { intent ->
                    UCrop.getOutput(intent)?.let {
                        mCropCallBack?.invoke(it)
                    } ?: Log.e(TAG, "裁剪失败")
                } ?: Log.e(TAG, "裁剪失败")
                // 必须置为null   否则有内存泄漏的风险
                mCropCallBack = null
            }
        }
    } else if (resultCode == UCrop.RESULT_ERROR) {
        data?.let {
            UCrop.getError(it)?.printStackTrace()
        }
        mCropCallBack = null
    }
}

/**
 * Activity中启动裁剪
 */
fun Activity.startCrop(sourceUri: Uri, callBack: ((res: Uri) -> Unit)? = null) {
    mCropCallBack = callBack
    val options = UCrop.Options()
    val themeColor = ContextCompat.getColor(this, R.color.theme_blue)
    options.setToolbarColor(themeColor)
    options.setStatusBarColor(themeColor)
    // 隐藏底部控制条
    options.setHideBottomControls(true)
    UCrop
            .of(sourceUri, Uri.fromFile(File(this.filesDir, "destination-crop-bitmap.jpg")))
            .withAspectRatio(1f, 1f)
            .withOptions(options)
            .withMaxResultSize(500, 500)
            .start(this)
}

/**
 * 处理裁剪返回值
 */
fun Activity.handleCropResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode == Activity.RESULT_OK) {
        when (requestCode) {
            UCrop.REQUEST_CROP -> {
                //裁剪成功
                data?.let { intent ->
                    UCrop.getOutput(intent)?.let {
                        mCropCallBack?.invoke(it)
                    } ?: Log.e(TAG, "裁剪失败")
                } ?: Log.e(TAG, "裁剪失败")
                mCropCallBack = null
            }
        }
    } else if (resultCode == UCrop.RESULT_ERROR) {
        data?.let {
            UCrop.getError(it)?.printStackTrace()
        }
        mCropCallBack = null
    }
}
