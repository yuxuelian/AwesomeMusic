package com.kaibo.core.util

import android.app.Activity
import android.support.v4.app.Fragment
import com.kaibo.core.R
import org.jetbrains.anko.internals.AnkoInternals

/**
 * @author kaibo
 * @date 2018/7/30 15:31
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

inline fun <reified T : Activity> Fragment.animStartActivity(vararg params: Pair<String, Any?>) {
    activity?.let {
        this.startActivity(AnkoInternals.createIntent(it, T::class.java, params))
        it.overridePendingTransition(R.anim.translation_right_in, R.anim.translation_right_out)
    }
}

inline fun <reified T : Activity> Fragment.animStartActivityForResult(requestCode: Int, vararg params: Pair<String, Any?>) {
    activity?.let {
        this.startActivityForResult(AnkoInternals.createIntent(it, T::class.java, params), requestCode)
        it.overridePendingTransition(R.anim.translation_right_in, R.anim.translation_right_out)
    }
}