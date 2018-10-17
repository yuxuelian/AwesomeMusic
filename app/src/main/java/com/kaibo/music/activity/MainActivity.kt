package com.kaibo.music.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.kaibo.core.activity.BaseActivity
import com.kaibo.music.R
import com.kaibo.music.fragment.home.HomeFragment
import com.kaibo.music.fragment.home.MeFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author kaibo
 * @date 2018/10/9 11:02
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class MainActivity : BaseActivity() {

    var currentItem
        get() = mainPager.currentItem
        set(value) {
            mainPager.currentItem = value
        }

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        val fragments: List<Fragment> = listOf(MeFragment(), HomeFragment())
        mainPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {

            override fun getItem(position: Int) = fragments[position]

            override fun getCount() = fragments.size
        }
        // 默认选中到1页
        mainPager.currentItem = 1
    }

}
