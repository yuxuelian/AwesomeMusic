package com.kaibo.music.activity

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.activity.BaseActivity
import com.kaibo.core.util.statusBarHeight
import com.kaibo.music.R
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.android.synthetic.main.include_play_top_layout.*

/**
 * @author kaibo
 * @date 2018/10/15 16:18
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class PlayActivity : BaseActivity() {

    override fun getLayoutRes() = R.layout.activity_play

    override fun initOnCreate(savedInstanceState: Bundle?) {
        // 空出StatusBar的高度
        playTopLayout.layoutParams = playTopLayout.layoutParams.apply {
            (this as ConstraintLayout.LayoutParams).topMargin = statusBarHeight
        }

        backBtn.clicks().`as`(bindLifecycle()).subscribe {
            onBackPressed()
        }
    }

}