package com.kaibo.music.fragment.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.util.easyClick
import com.kaibo.core.util.statusBarHeight
import com.kaibo.music.R
import com.kaibo.music.fragment.home.tab.MineFragment
import com.kaibo.music.fragment.home.tab.RankFragment
import com.kaibo.music.fragment.home.tab.RecommendFragment
import com.kaibo.music.fragment.home.tab.SingerFragment
import com.kaibo.music.fragment.search.SearchFragment
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

    companion object {
        fun newInstance(arguments: Bundle = Bundle()): HomeFragment {
            return HomeFragment().apply {
                this.arguments = arguments
            }
        }
    }

    override fun getLayoutRes() = R.layout.fragment_home

    override fun initViewCreated(savedInstanceState: Bundle?) {
        super.initViewCreated(savedInstanceState)
        appBarLayout.setPadding(0, context!!.statusBarHeight, 0, 0)

        // 启动搜索Fragment
        search.easyClick(bindLifecycle()).subscribe {
            // 启动搜索Fragment
            start(SearchFragment.newInstance())
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