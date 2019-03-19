package com.kaibo.core.dialog

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import com.github.ybq.android.spinkit.style.FadingCircle
import com.kaibo.core.R
import kotlinx.android.synthetic.main.dialog_loading.*

/**
 * @author kaibo
 * @date 2018/6/27 14:29
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class LoadingDialog : BaseDialog() {

    override fun getLayoutRes() = R.layout.dialog_loading

    override fun initViewCreated(savedInstanceState: Bundle?) {
        super.initViewCreated(savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        isCancelable = false
        //屏蔽返回键
        dialog?.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
        val doubleBounce = FadingCircle()
        spin_kit.setIndeterminateDrawable(doubleBounce)
    }

    fun show(manager: FragmentManager) {
        show(manager, toString())
    }

    fun hide() {
        dismiss()
    }

    override fun getSize() = Pair(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
}
