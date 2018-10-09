package com.kaibo.core.dialog

import android.os.Bundle
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.R
import kotlinx.android.synthetic.main.dialog_msg.*
import org.jetbrains.anko.support.v4.dip

/**
 * @author kaibo
 * @date 2018/6/28 9:38
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */


class MessageDialog : BaseDialog() {

    override fun getLayoutRes() = R.layout.dialog_msg

    var cancelListener: (() -> Unit)? = null
    var confirmListener: (() -> Unit)? = null
    var msgText: String = "提示消息"

    override fun initViewCreated(savedInstanceState: Bundle?) {
        super.initViewCreated(savedInstanceState)
        msg_text.text = msgText
        cancel_btn.clicks().subscribe {
            cancelListener?.invoke()
            dismiss()
        }
        confirm_btn.clicks().subscribe {
            confirmListener?.invoke()
            dismiss()
        }
    }

    override fun getSize() = Pair(dip(280), dip(160))
}