package com.kaibo.core.dialog

import android.os.Bundle
import android.view.Gravity
import com.kaibo.core.R

/**
 * @author kaibo
 * @date 2018/7/31 15:22
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
abstract class BaseSheetDialog : BaseDialog() {

    override fun initViewCreated(savedInstanceState: Bundle?) {
        //弹出动画
        dialog.window.setWindowAnimations(R.style.dialogBottomSheet)
        //设置Dialog的位置在底部显示
        dialog.window.attributes.gravity = Gravity.BOTTOM
    }

}
