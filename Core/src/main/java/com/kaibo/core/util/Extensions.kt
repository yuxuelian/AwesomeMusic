package com.kaibo.core.util

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import com.kaibo.core.R
import com.kaibo.core.adapter.ItemDividerDecoration
import org.jetbrains.anko.dip

/**
 * @author kaibo
 * @date 2018/7/5 15:32
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

/**
 * 设置默认的RecyclerView的分割线
 */
fun RecyclerView.addDefaultDecoration(decoration: Drawable = ColorDrawable(ContextCompat.getColor(context, R.color.color_303030)),
                                      heightPx: Int = dip(1)) {
    this.addItemDecoration(ItemDividerDecoration(decoration, heightPx))
}
