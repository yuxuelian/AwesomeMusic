package com.kaibo.core.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.kaibo.core.R

/**
 * @author:Administrator
 * @date:2018/4/2 0002 上午 10:33
 * GitHub:
 * email:
 * description:
 */

abstract class BaseDialog : DialogFragment() {

    private lateinit var fragmentActivity: FragmentActivity

    protected val mActivity
        get() = fragmentActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentActivity = context as FragmentActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutRes(), container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        //设置为无标题
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //去掉默认的背景
        dialog?.window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context!!, R.color.transparent)))
        initViewCreated(savedInstanceState)
    }

    protected open fun initViewCreated(savedInstanceState: Bundle?) {
        //设置一个默认的对话框动画,默认是淡入淡出,可重写
        dialog?.window?.setWindowAnimations(R.style.dialogShadow)
    }

    /**
     * 返回布局文件
     */
    abstract fun getLayoutRes(): Int

    /**
     * 设置对话框的大小
     */
    override fun onResume() {
        super.onResume()
        val size = getSize()
        dialog?.window?.setLayout(size.first, size.second)
    }

    /**
     * 返回对话框的大小    Pair<宽,高>
     */
    abstract fun getSize(): Pair<Int, Int>

}