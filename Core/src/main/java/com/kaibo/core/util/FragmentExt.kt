package com.kaibo.core.util

import android.app.Activity
import com.kaibo.core.R

/**
 * @author kaibo
 * @date 2018/7/30 15:31
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

inline fun <reified T : Activity> androidx.fragment.app.Fragment.startActivity(vararg params: Pair<String, Any?>) =
        startActivity(requireActivity().createIntent(T::class.java, params))

inline fun <reified T : Activity> androidx.fragment.app.Fragment.startActivityForResult(requestCode: Int, vararg params: Pair<String, Any?>) =
        startActivityForResult(requireActivity().createIntent(T::class.java, params), requestCode)

inline fun <reified T : Activity> androidx.fragment.app.Fragment.animStartActivity(vararg params: Pair<String, Any?>) {
    activity?.let {
        it.startActivity<T>(*params)
        it.overridePendingTransition(R.anim.translation_right_in, R.anim.translation_right_out)
    }
}

inline fun <reified T : Activity> androidx.fragment.app.Fragment.animStartActivityForResult(requestCode: Int, vararg params: Pair<String, Any?>) {
    activity?.let {
        // 这里只能用 this 去启动  否则onActivityResult得不到执行
        this.startActivityForResult(it.createIntent(T::class.java, params), requestCode)
        it.overridePendingTransition(R.anim.translation_right_in, R.anim.translation_right_out)
    }
}