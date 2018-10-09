package com.kaibo.core.toast

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.kaibo.core.R
import kotlinx.android.synthetic.main.toast_layout.view.*
import org.jetbrains.anko.backgroundResource


/**
 * @author:Administrator
 * @date:2018/5/14 0014 上午 9:09
 * @GitHub:https://github.com/yuxuelian
 * @email:
 * @description:
 */

object ToastUtils {

    private lateinit var toast: Toast

    private var isInit = false

    @SuppressLint("ShowToast")
    fun init(context: Context) {
        isInit = true
        toast = Toast(context)
        toast.view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null, false)
    }

    @ColorInt
    private val DEFAULT_TEXT_COLOR = Color.WHITE

    @DrawableRes
    private val INFO_COLOR = R.drawable.corners_info_toast_bg

    @DrawableRes
    private val SUCCESS_COLOR = R.drawable.corners_success_toast_bg

    @DrawableRes
    private val WARNING_COLOR = R.drawable.corners_warning_toast_bg

    @DrawableRes
    private val ERROR_COLOR = R.drawable.corners_error_toast_bg

    @JvmOverloads
    fun showInfo(msg: CharSequence?, @ColorInt textColor: Int = DEFAULT_TEXT_COLOR, @DrawableRes tintColor: Int = INFO_COLOR, showIcon: Boolean = true) {
        if (!isInit) {
            throw IllegalStateException("please call init method")
        }
        set(showIcon, textColor, tintColor, msg, R.drawable.icon_info)
    }

    @JvmOverloads
    fun showSuccess(msg: CharSequence?, @ColorInt textColor: Int = DEFAULT_TEXT_COLOR, @DrawableRes tintColor: Int = SUCCESS_COLOR, showIcon: Boolean = true) {
        if (!isInit) {
            throw IllegalStateException("please call init method")
        }
        set(showIcon, textColor, tintColor, msg, R.drawable.icon_success)
    }

    @JvmOverloads
    fun showWarning(msg: CharSequence?, @ColorInt textColor: Int = DEFAULT_TEXT_COLOR, @DrawableRes tintColor: Int = WARNING_COLOR, showIcon: Boolean = true) {
        if (!isInit) {
            throw IllegalStateException("please call init method")
        }
        set(showIcon, textColor, tintColor, msg, R.drawable.icon_warning)
    }

    @JvmOverloads
    fun showError(msg: CharSequence?, @ColorInt textColor: Int = DEFAULT_TEXT_COLOR, @DrawableRes tintColor: Int = ERROR_COLOR, showIcon: Boolean = true) {
        if (!isInit) {
            throw IllegalStateException("please call init method")
        }
        set(showIcon, textColor, tintColor, msg, R.drawable.icon_error)
    }

    private fun set(showIcon: Boolean, @ColorInt textColor: Int, @DrawableRes bgDrawable: Int, msg: CharSequence?, @DrawableRes icon: Int) {
        toast.view.backgroundResource = bgDrawable

        toast.view.toast_icon.visibility = if (showIcon) View.VISIBLE else View.GONE
        toast.view.toast_icon.setImageResource(icon)
        toast.view.toast_text.setTextColor(textColor)
        toast.view.toast_text.text = msg
        toast.show()
    }
}