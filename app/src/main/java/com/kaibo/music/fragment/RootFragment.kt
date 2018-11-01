package com.kaibo.music.fragment

import android.os.Bundle
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.music.R
import com.kaibo.music.fragment.home.HomeFragment
import com.kaibo.music.fragment.home.MiniPlayerFragment

/**
 * @author kaibo
 * @date 2018/11/1 10:01
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class RootFragment : BaseFragment() {

    companion object {
        fun newInstance(arguments: Bundle = Bundle()): RootFragment {
            return RootFragment().apply {
                this.arguments = arguments
            }
        }
    }

    override fun getLayoutRes() = R.layout.fragment_root

    override fun initViewCreated(savedInstanceState: Bundle?) {
        super.initViewCreated(savedInstanceState)
        // 加载 HomeFragment
        if (findChildFragment(HomeFragment::class.java) == null) {
            loadRootFragment(R.id.homeContainer, HomeFragment.newInstance())
        }
        // 加载 MiniPlayerFragment
        if (findChildFragment(MiniPlayerFragment::class.java) == null) {
            loadRootFragment(R.id.miniPlayerContainer, MiniPlayerFragment.newInstance())
        }
    }

}