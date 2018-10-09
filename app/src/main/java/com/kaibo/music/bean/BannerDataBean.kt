package com.kaibo.music.bean

import android.support.annotation.DrawableRes

/**
 * @author kaibo
 * @date 2018/10/9 11:02
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

data class BannerDataBean(
        val imgUrl: String = "",
        @DrawableRes
        val testDrawable: Int = 0
)