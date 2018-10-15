package com.kaibo.music.fragment.home

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.util.animInStartActivity
import com.kaibo.core.util.statusBarHeight
import com.kaibo.music.R
import com.kaibo.music.activity.MainActivity
import com.kaibo.music.activity.PlayActivity
import com.kaibo.music.activity.SearchActivity
import com.kaibo.music.fragment.rank.RankFragment
import com.kaibo.music.fragment.recommend.RecommendFragment
import com.kaibo.music.fragment.singer.SingerFragment
import kotlinx.android.synthetic.main.fragment_home_layout.*
import kotlinx.android.synthetic.main.include_bottom_play_layout.*

/**
 * @author kaibo
 * @date 2018/10/15 14:41
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class HomeFragment : BaseFragment() {

    override fun getLayoutRes() = R.layout.fragment_home_layout

    override fun initViewCreated(savedInstanceState: Bundle?) {
        appBarLayout.setPadding(0, activity!!.statusBarHeight, 0, 0)
        initViewPager()
        // 点击左上角的用户按钮
        user.clicks().`as`(bindLifecycle()).subscribe {
            (activity as? MainActivity)?.currentItem = 0
        }
        // 点击右上角的搜索按钮
        search.clicks().`as`(bindLifecycle()).subscribe {
            activity?.animInStartActivity<SearchActivity>()
        }
        // 点击底部的播放条
        bottom_play_layout.clicks().`as`(bindLifecycle()).subscribe {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val intent = Intent(activity, PlayActivity::class.java)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity, songImg, "shareSongImage").toBundle())
            } else {
                activity?.animInStartActivity<PlayActivity>()
            }

        }
    }

    private fun initViewPager() {
        val pageTitles: Array<String> = resources.getStringArray(R.array.home_tab_array)
        val fragments = listOf<Fragment>(RecommendFragment(), SingerFragment(), RankFragment())
        viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(position: Int) = fragments[position]

            override fun getCount() = fragments.size

            override fun getPageTitle(position: Int) = pageTitles[position]
        }
        viewPager.offscreenPageLimit = 4
        tabLayout.setupWithViewPager(viewPager)
    }

}