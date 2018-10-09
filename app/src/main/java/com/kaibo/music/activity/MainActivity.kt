package com.kaibo.music.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import com.kaibo.core.activity.BaseActivity
import com.kaibo.core.util.statusBarHeight
import com.kaibo.music.R
import com.kaibo.music.fragment.rank.RankFragment
import com.kaibo.music.fragment.recommend.RecommendFragment
import com.kaibo.music.fragment.search.SearchFragment
import com.kaibo.music.fragment.singer.SingerFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_title.*

class MainActivity : BaseActivity() {

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        appBarLayout.setPadding(0, statusBarHeight, 0, 0)
        initViewPager()
    }

    private fun initViewPager() {
        val pageTitles: Array<String> = resources.getStringArray(R.array.home_tab_array)
        val fragments = listOf<Fragment>(RecommendFragment(), SingerFragment(), RankFragment(), SearchFragment())
        viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int) = fragments[position]

            override fun getCount() = fragments.size

            override fun getPageTitle(position: Int) = pageTitles[position]
        }
        tabLayout.setupWithViewPager(viewPager)
    }
}
