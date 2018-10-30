package com.kaibo.core.util

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kaibo.core.R
import com.kaibo.core.adapter.ItemDividerDecoration
import org.jetbrains.anko.dip

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
    val oldDrawable = this.drawable
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
    val objectAnimator: ObjectAnimator
    objectAnimator = ObjectAnimator.ofInt(this, "backgroundColor", olderColor, newColor).setDuration(800)
    objectAnimator.setEvaluator(ArgbEvaluator())
    objectAnimator.start()
}
