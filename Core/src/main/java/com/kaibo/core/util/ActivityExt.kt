package com.kaibo.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import com.kaibo.core.R
import java.io.File

/**
 * @author kaibo
 * @date 2018/6/26 10:31
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

inline fun <reified T : Activity> Context.startActivity(vararg params: Pair<String, Any?>) =
        startActivity(this.createIntent(T::class.java, params))

inline fun <reified T : Activity> Activity.startActivityForResult(requestCode: Int, vararg params: Pair<String, Any?>) =
        startActivityForResult(this.createIntent(T::class.java, params), requestCode)

inline fun <reified T : Activity> Activity.animStartActivity(vararg params: Pair<String, Any?>) {
    startActivity<T>(*params)
    overridePendingTransition(R.anim.translation_right_in, R.anim.translation_right_out)
}

inline fun <reified T : Activity> Activity.animStartActivityForResult(requestCode: Int, vararg params: Pair<String, Any?>) {
    startActivityForResult<T>(requestCode, *params)
    overridePendingTransition(R.anim.translation_right_in, R.anim.translation_right_out)
}

fun androidx.fragment.app.FragmentActivity.addFragmentToActivity(@IdRes frameId: Int, fragment: androidx.fragment.app.Fragment) {
    supportFragmentManager.beginTransaction().replace(frameId, fragment).commit()
}

/**
 * 隐藏软键盘
 */

fun Activity.hideSoftInput() {
    (this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(this.currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

/**
 * 将新增的图片添加到系统的路径去
 */
private fun Context.galleryAddPic(photoFile: File) {
    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    //mCurrentPhotoPath即文件的路径
    val contentUri = Uri.fromFile(photoFile)
    mediaScanIntent.data = contentUri
    this.sendBroadcast(mediaScanIntent)
}

fun Activity.setActivityBrightness(paramFloat: Float) {
    val params: WindowManager.LayoutParams = window.attributes
    params.screenBrightness = paramFloat
    window.attributes = params
}

fun Activity.resetActivityBrightness() {
    val params: WindowManager.LayoutParams = window.attributes
    params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    window.attributes = params
}
