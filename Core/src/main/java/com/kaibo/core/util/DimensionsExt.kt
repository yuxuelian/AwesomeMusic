package com.kaibo.core.util

import android.content.Context
import android.view.View
import androidx.annotation.DimenRes

/**
 * @author kaibo
 * @date 2018/10/29 10:49
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

// 能获取到Content的情况
inline fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()

inline fun Context.dip(value: Float): Int = (value * resources.displayMetrics.density).toInt()
inline fun Context.sp(value: Int): Int = (value * resources.displayMetrics.scaledDensity).toInt()
inline fun Context.sp(value: Float): Int = (value * resources.displayMetrics.scaledDensity).toInt()
inline fun Context.px2dip(px: Int): Float = px.toFloat() / resources.displayMetrics.density
inline fun Context.px2sp(px: Int): Float = px.toFloat() / resources.displayMetrics.scaledDensity
inline fun Context.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)

// View中使用
inline fun View.dip(value: Int): Int = context.dip(value)

inline fun View.dip(value: Float): Int = context.dip(value)
inline fun View.sp(value: Int): Int = context.sp(value)
inline fun View.sp(value: Float): Int = context.sp(value)
inline fun View.px2dip(px: Int): Float = context.px2dip(px)
inline fun View.px2sp(px: Int): Float = context.px2sp(px)
inline fun View.dimen(@DimenRes resource: Int): Int = context.dimen(resource)

// SupportFragment中使用
inline fun androidx.fragment.app.Fragment.dip(value: Int): Int = requireActivity().dip(value)

inline fun androidx.fragment.app.Fragment.dip(value: Float): Int = requireActivity().dip(value)
inline fun androidx.fragment.app.Fragment.sp(value: Int): Int = requireActivity().sp(value)
inline fun androidx.fragment.app.Fragment.sp(value: Float): Int = requireActivity().sp(value)
inline fun androidx.fragment.app.Fragment.px2dip(px: Int): Float = requireActivity().px2dip(px)
inline fun androidx.fragment.app.Fragment.px2sp(px: Int): Float = requireActivity().px2sp(px)
inline fun androidx.fragment.app.Fragment.dimen(resource: Int): Int = requireActivity().dimen(resource)
