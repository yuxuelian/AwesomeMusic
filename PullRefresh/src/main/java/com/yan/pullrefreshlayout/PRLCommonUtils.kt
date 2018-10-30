package com.yan.pullrefreshlayout

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.ListView

import androidx.core.widget.ListViewCompat

/**
 * Created by yan on 2017/5/21
 */
object PRLCommonUtils {

    /**
     * code from SwipeRefreshLayout
     *
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    fun canChildScrollUp(targetView: View?): Boolean {
        if (targetView == null) {
            return false
        }
        return if (targetView is ListView) {
            ListViewCompat.canScrollList(targetView, -1)
        } else {
            targetView.canScrollVertically(-1)
        }
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll down. Override this if the child view is a custom view.
     */
    fun canChildScrollDown(targetView: View?): Boolean {
        if (targetView == null) {
            return false
        }
        return if (targetView is ListView) {
            ListViewCompat.canScrollList(targetView, 1)
        } else {
            targetView.canScrollVertically(1)
        }
    }

    /**
     * common utils
     */
    fun getWindowHeight(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun dipToPx(context: Context, value: Float): Int {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics).toInt()
    }
}