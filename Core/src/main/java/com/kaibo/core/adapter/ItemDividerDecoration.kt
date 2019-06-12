package com.kaibo.core.adapter

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author kaibo
 * @date 2018/7/4 14:47
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class ItemDividerDecoration(private val mDrawable: Drawable, private val width: Int) : RecyclerView.ItemDecoration() {

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)
        this.drawHorizontalLine(canvas, parent)
    }

    /**
     * 画横线, 这里的parent其实是显示在屏幕显示的这部分
     * @param canvas
     * @param parent
     */
    private fun drawHorizontalLine(canvas: Canvas, parent: RecyclerView) {
        //分割线左边的偏移
        val left = parent.paddingLeft
        //分割线右边的偏移
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        repeat(childCount - 1) {
            val child: View = parent.getChildAt(it)
            //获得child的布局信息
            val params: RecyclerView.LayoutParams = child.layoutParams as RecyclerView.LayoutParams
            //分割线顶部偏移
            val top = child.bottom + params.bottomMargin
            //分割线底部偏移
            val bottom = top + width
            //设置分割线需要绘制的位置
            mDrawable.setBounds(left, top, right, bottom)
            //绘制
            mDrawable.draw(canvas)
        }
    }

    /**
     * @param outRect 边界
     * @param view    recyclerView ItemView
     * @param parent  recyclerView
     * @param state   recycler 内部数据管理
     */
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        //Item布局进行偏移
        outRect.set(0, 0, 0, width)
    }
}