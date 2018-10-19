package com.kaibo.music.activity

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.activity.CoreActivity
import com.kaibo.core.util.animInStartActivity
import com.kaibo.core.util.statusBarHeight
import com.kaibo.music.R
import com.kaibo.music.fragment.mine.MineFragment
import com.kaibo.music.fragment.rank.RankFragment
import com.kaibo.music.fragment.recommend.RecommendFragment
import com.kaibo.music.fragment.singer.SingerFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_home_title.*
import kotlinx.android.synthetic.main.include_mini_play.*

/**
 * @author kaibo
 * @date 2018/10/9 11:02
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class MainActivity : CoreActivity() {

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        appBarLayout.setPadding(0, statusBarHeight, 0, 0)
        initViewPager()

        // 点击右上角的搜索按钮
        search.clicks().`as`(bindLifecycle()).subscribe {
            animInStartActivity<SearchActivity>()
        }
        // 点击底部的播放条
        mine_play_layout.clicks().`as`(bindLifecycle()).subscribe {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val intent = Intent(this, PlayerActivity::class.java)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this, songImg, getString(R.string.transition_share_song_img)).toBundle())
            } else {
                animInStartActivity<PlayerActivity>()
            }
        }
    }

    private fun initViewPager() {
        val pageTitles: Array<String> = resources.getStringArray(R.array.home_tab_array)
        val fragments = listOf<Fragment>(RecommendFragment(), SingerFragment(), RankFragment(), MineFragment())
        mainPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int) = fragments[position]

            override fun getCount() = fragments.size

            override fun getPageTitle(position: Int) = pageTitles[position]
        }
        mainPager.offscreenPageLimit = 4
        tabLayout.setupWithViewPager(mainPager)
    }

}
