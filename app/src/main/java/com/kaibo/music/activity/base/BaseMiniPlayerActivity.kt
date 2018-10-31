package com.kaibo.music.activity.base

import android.os.Bundle
import com.kaibo.core.util.addFragmentToActivity
import com.kaibo.music.fragment.MiniPlayerFragment

/**
 * @author kaibo
 * @date 2018/10/31 14:38
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

abstract class BaseMiniPlayerActivity : BaseActivity() {

    protected abstract val mineContainer: Int

    override fun initOnCreate(savedInstanceState: Bundle?) {
        super.initOnCreate(savedInstanceState)
        addFragmentToActivity(mineContainer, MiniPlayerFragment())
    }
}
