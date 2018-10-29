package com.kaibo.core.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment

/**
 * @author:Administrator
 * @date:2018/4/8 0008 下午 12:52
 * GitHub:
 * email:
 * description:对android屏幕尺寸等相关操作的扩展方法
 */


/**
 * 状态栏的高度
 */
val Context.statusBarHeight
    get() = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))

/**
 * 设置沉浸式
 * isLight是否对状态栏颜色变黑
 */
fun Activity.immersive(isLight: Boolean) {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
            with(window) {
                //清除状态栏默认状态
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                // 不加下面这句话可能某些手机无法正常实现沉浸式
                addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                //SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 布局设置为全屏布局
                //SYSTEM_UI_FLAG_LAYOUT_STABLE
                //SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        if (isLight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        } else {
                            0
                        }
                statusBarColor = Color.TRANSPARENT
            }
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
            this.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        else -> {
            return
        }
    }
}

/**
 * 给Fragment中顶部的View设置状态栏高度的内边距
 */
fun Fragment.immersiveTopView(topView: View) {
    context?.let {
        topView.setPadding(0, it.statusBarHeight, 0, 0)
    }
}

val Context.deviceWidth
    get() = this.resources.displayMetrics.widthPixels

val Context.deviceHeight
    get() = this.resources.displayMetrics.heightPixels

fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

/**
 * ImageView使用Glide进行图片加载
 */
//fun ImageView.defaultLoadImage(url: String?) {
//    GlideApp
//            .with(context)
//            .load(url)
//            .diskCacheStrategy(DiskCacheStrategy.ALL)
//            .centerCrop()
//            .placeholder(R.drawable.ic_image_loading)
//            .error(R.drawable.ic_empty_picture)
//            .into(this)
//}
//
//fun ImageView.displayHigh(url: String?) {
//    GlideApp
//            .with(context)
//            .asBitmap()
//            .load(url)
//            .format(DecodeFormat.PREFER_ARGB_8888)
//            .diskCacheStrategy(DiskCacheStrategy.ALL)
//            .centerCrop()
//            .placeholder(R.drawable.ic_image_loading)
//            .error(R.drawable.ic_empty_picture)
//            .into(this)
//}