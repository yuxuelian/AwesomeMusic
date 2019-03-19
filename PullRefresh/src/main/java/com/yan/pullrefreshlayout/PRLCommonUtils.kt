package com.yishi.refresh

import android.view.View
import android.widget.ListView
import androidx.core.widget.ListViewCompat

/**
 * @author 56896
 * @date 2018/10/18 23:54
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
internal object PRLCommonUtils {

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
}