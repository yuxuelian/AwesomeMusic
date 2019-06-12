package com.kaibo.music.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.util.animStartActivity
import com.kaibo.core.util.bindLifecycle
import com.kaibo.core.util.easyClick
import com.kaibo.core.util.statusBarHeight
import com.kaibo.music.R
import com.kaibo.music.activity.SearchActivity
import com.kaibo.music.fragment.tab.MineFragment
import com.kaibo.music.fragment.tab.RankFragment
import com.kaibo.music.fragment.tab.RecommendFragment
import com.kaibo.music.fragment.tab.SingerFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.include_home_title.*

/**
 * @author kaibo
 * @date 2018/11/1 9:48
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class HomeFragment : BaseFragment() {

    override fun getLayoutRes() = R.layout.fragment_home

    override fun initViewCreated(savedInstanceState: Bundle?) {
        super.initViewCreated(savedInstanceState)
        appBarLayout.setPadding(0, statusBarHeight, 0, 0)

        // 启动搜索Fragment
        search.easyClick(bindLifecycle()).subscribe {
            animStartActivity<SearchActivity>()
        }

        initViewPager()
    }

    private fun initViewPager() {
        val pageTitles: Array<String> = resources.getStringArray(R.array.home_tab_array)
        val fragments = listOf<Fragment>(RecommendFragment(), SingerFragment(), RankFragment(), MineFragment())
        mainPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(position: Int) = fragments[position]

            override fun getCount() = fragments.size

            override fun getPageTitle(position: Int) = pageTitles[position]
        }
        mainPager.offscreenPageLimit = 4
        tabLayout.setupWithViewPager(mainPager)
    }
}