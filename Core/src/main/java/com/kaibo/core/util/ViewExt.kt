package com.kaibo.core.util

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.kaibo.core.R
import com.kaibo.core.adapter.ItemDividerDecoration
import java.lang.ref.WeakReference

/**
 * @author kaibo
 * @date 2018/9/21 15:50
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

fun ViewGroup.inflate(@LayoutRes resource: Int, attachToRoot: Boolean = false): View = LayoutInflater.from(context).inflate(resource, this, attachToRoot)

/**
 * 获取View上的截图
 */
fun View.screenshot(): Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
    draw(Canvas(it))
}

fun ImageView.startAlphaBitmap(bitmap: Bitmap) {
    val fromDrawable = this.drawable ?: ColorDrawable(Color.TRANSPARENT)
    val toDrawable = BitmapDrawable(resources, bitmap)
    val transitionDrawable = TransitionDrawable(arrayOf(fromDrawable, toDrawable))
    this.setImageDrawable(transitionDrawable)
    transitionDrawable.startTransition(300)
}

fun ImageView.startAlphaDrawable(toDrawable: Drawable) {
    val fromDrawable = this.drawable ?: ColorDrawable(Color.TRANSPARENT)
    val td = TransitionDrawable(arrayOf(fromDrawable, toDrawable))
    this.setImageDrawable(td)
    td.startTransition(300)
}

/**
 * 给ImageView上已经存在的Drawable染色
 */
fun ImageView.setTintDrawable(@ColorInt color: Int) {
    this.drawable?.let {
        DrawableCompat.setTintList(it, ColorStateList.valueOf(color))
        this.setImageDrawable(it)
    }
}

/**
 * 缓存bitmap  并且使用弱引用的方式(防止oom)
 */
private val mLruBitmapCache = LruCache<String, WeakReference<Bitmap>>(20)

/**
 * 设置默认的RecyclerView的分割线
 */
fun RecyclerView.setDecoration(decoration: Drawable = ColorDrawable(context.getCompatColor(R.color.color_303030)), heightPx: Int = dip(1)) {
    this.addItemDecoration(ItemDividerDecoration(decoration, heightPx))
}

/**
 * 设置默认的RecyclerView的分割线
 */
fun RecyclerView.addDefaultDecoration(decoration: Drawable = ColorDrawable(ContextCompat.getColor(context, R.color.color_303030)),
                                      heightPx: Int = dip(1)) {
    this.addItemDecoration(ItemDividerDecoration(decoration, heightPx))
}

/**
 * 图片渐变切换动画
 */
fun ImageView.startTransition(targetBitmap: Bitmap) {
    this.startTransition(BitmapDrawable(resources, targetBitmap))
}

/**
 * 图片渐变切换动画
 */
fun ImageView.startTransition(targetDrawable: Drawable) {
    val oldDrawable: Drawable? = this.drawable
    val oldBitmapDrawable = when (oldDrawable) {
        null -> ColorDrawable(Color.TRANSPARENT)
        is TransitionDrawable -> oldDrawable.getDrawable(1)
        else -> oldDrawable
    }
    val td = TransitionDrawable(arrayOf(oldBitmapDrawable, targetDrawable))
    this.setImageDrawable(td)
    // 启动动画
    td.startTransition(1000)
}

/**
 * 颜色渐变动画
 */
fun View.startTransition(newColor: Int) {
    val olderColor = (background as ColorDrawable).color
    val objectAnimator = ObjectAnimator.ofInt(this, "backgroundColor", olderColor, newColor).setDuration(800)
    objectAnimator.setEvaluator(ArgbEvaluator())
    objectAnimator.start()
}

