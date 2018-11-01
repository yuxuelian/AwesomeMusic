package com.kaibo.music.fragment

import android.os.Bundle
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.music.R

/**
 * @author kaibo
 * @date 2018/11/1 9:52
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class SplashFragment : BaseFragment() {

    companion object {
        fun newInstance(arguments: Bundle = Bundle()): SplashFragment {
            return SplashFragment().apply {
                this.arguments = arguments
            }
        }
    }

    override fun getLayoutRes() = R.layout.fragment_splash

}