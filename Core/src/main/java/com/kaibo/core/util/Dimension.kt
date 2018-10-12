package com.kaibo.core.util

import androidx.annotation.DimenRes
import androidx.fragment.app.Fragment
import org.jetbrains.anko.*

/**
 * @author kaibo
 * @date 2018/10/12 16:36
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

inline fun Fragment.dip(value: Int): Int = activity?.dip(value) ?: 0

inline fun Fragment.dip(value: Float): Int = activity?.dip(value) ?: 0
inline fun Fragment.sp(value: Int): Int = activity?.dip(value) ?: 0
inline fun Fragment.sp(value: Float): Int = activity?.sp(value) ?: 0
inline fun Fragment.px2dip(px: Int): Float = activity?.px2dip(px) ?: 0F
inline fun Fragment.px2sp(px: Int): Float = activity?.px2sp(px) ?: 0F
inline fun Fragment.dimen(@DimenRes resource: Int): Int = activity?.dimen(resource) ?: 0