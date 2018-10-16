package com.kaibo.music.activity.base

import com.kaibo.core.activity.BaseActivity

/**
 * @author kaibo
 * @date 2018/10/15 15:41
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

abstract class BaseAnimActivity:BaseActivity(){
    /**
     * 带动画返回
     */
    override fun onBackPressed() {
        animOutFinish()
    }
}