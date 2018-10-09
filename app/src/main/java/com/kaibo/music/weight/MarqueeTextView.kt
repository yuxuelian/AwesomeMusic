package com.kaibo.music.weight

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.text.TextUtils
import android.util.AttributeSet

/**
 * @author:Administrator
 * @date:2018/4/3 0003 上午 11:06
 * GitHub:
 * email:
 * description:跑马灯 TextView
 */

class MarqueeTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        setSingleLine()    //设置只显示一行,才会有跑马灯效果
        ellipsize = TextUtils.TruncateAt.MARQUEE //设置跑马灯效果
        marqueeRepeatLimit = -1  //设置为-1相当于marquee_forever
//        isClickable = true //设置可点击,消耗掉点击事件
    }

    override fun isFocused() = true
}
