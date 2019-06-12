package com.kaibo.core.dialog

import android.os.Bundle
import android.view.WindowManager
import android.widget.DatePicker
import com.kaibo.core.R
import com.kaibo.core.util.bindLifecycle
import com.kaibo.core.util.easyClick
import kotlinx.android.synthetic.main.dialog_date_picker.*

/**
 * @author kaibo
 * @date 2018/8/3 12:47
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class DatePickerDialog : BaseSheetDialog() {

    override fun getLayoutRes() = R.layout.dialog_date_picker

    var confirmClickListener: ((year: Int, month: Int, dayOfMonth: Int) -> Unit)? = null

    override fun getSize(): Pair<Int, Int> {
        return Pair(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun initViewCreated(savedInstanceState: Bundle?) {
        super.initViewCreated(savedInstanceState)
        cancel.easyClick(bindLifecycle()).subscribe {
            dismiss()
        }

        confirm.easyClick(bindLifecycle()).subscribe {
            dismiss()
            confirmClickListener?.invoke(date_picker.year, date_picker.month, date_picker.dayOfMonth)
        }

        //禁止弹出软键盘
        date_picker.descendantFocusability = DatePicker.FOCUS_BLOCK_DESCENDANTS
        //设置最大日期
        arguments?.let {
            //设置最小值
            val minDate = it.getLong("minDate")
            if (minDate != 0L) {
                date_picker.minDate = minDate
            }
            //设置最大值
            val maxDate = it.getLong("maxDate")
            if (maxDate != 0L) {
                date_picker.maxDate = maxDate
            }
        }
    }
}
